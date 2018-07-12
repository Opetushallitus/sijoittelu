package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiPeruuntunutAloituspaikatTaynna;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;

/**
 * Tämä prosessori on ajettava ennen <code>PostSijoitteluProcessorAsetaSivssnov</code> :ia, jotta tämä voi tunnistaa
 * tilanteen, jossa ollaan ajamassa ensimmäistä sijoittelua sen jälkeen kun varasijasäännöt ovat astuneet voimaan.
 *
 * @see PostSijoitteluProcessorAsetaSivssnov
 */
public class PostSijoitteluProcessorPeruunnutaRajatunVarasijataytonHakemuksetJotkaEivatMahduVaralle implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorPeruunnutaRajatunVarasijataytonHakemuksetJotkaEivatMahduVaralle.class);

    @Override
    public void process(SijoitteluajoWrapper ajoWrapper) {
        LOG.debug("Haun " + ajoWrapper.getSijoitteluajo().getHakuOid() + " varasijasäännöt voimassa: " +
            ajoWrapper.getVarasijaSaannotAstuvatVoimaan() + " -- " + ajoWrapper.getVarasijaTayttoPaattyy());
        if (!ajoWrapper.varasijaSaannotVoimassa()) {
            LOG.debug("Haun " + ajoWrapper.getSijoitteluajo().getHakuOid() +
                " varasijasäännöt eivät ole voimassa, joten ei tehdä mitään.");
            return;
        }

        ajoWrapper.getHakukohteet().forEach(h -> h.getValintatapajonot().forEach(j -> process(j)));
    }

    private void process(ValintatapajonoWrapper jonoWrapper) {
        Valintatapajono jono = jonoWrapper.getValintatapajono();
        if (jono.vapaaVarasijataytto()) {
            return;
        }
        SijoitteluajoWrapper sijoitteluajoWrapper = jonoWrapper.getHakukohdeWrapper().getSijoitteluajoWrapper();
        assertSijoiteltuEnnenVarasijataytonLoppumista(jonoWrapper, sijoitteluajoWrapper);
        
        if (Boolean.TRUE.equals(jono.getEiVarasijatayttoa())) {
            if(sijoitteluajoWrapper.isKKHaku()) {
                jonoWrapper.getHakemukset().stream()
                        .filter(h -> h.isVaralla() && h.isTilaVoidaanVaihtaa())
                        .forEach(h -> asetaTilaksiPeruuntunutAloituspaikatTaynna(h));
            }
            return;
        }

        if (jono.rajoitettuVarasijaTaytto()) {
            if(jono.getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()) {
                int viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija = jonoWrapper.getHakemukset().stream()
                        .filter(h -> h.getHakemus().getEdellinenTila() == HakemuksenTila.VARALLA)
                        .filter(h -> h.getHakemus().getTila() != HakemuksenTila.HYLATTY)
                        .sorted(Comparator.comparing(h -> ((HakemusWrapper) h).getHakemus().getJonosija()).reversed())
                        .map(h -> h.getHakemus().getJonosija())
                        .findFirst()
                        .orElse(0);

                jonoWrapper.getHakemukset().stream()
                        .filter(HakemusWrapper::isVaralla)
                        .filter(h -> h.getHakemus().getJonosija() > viimeisenEdellisessaSijoittelussaVarallaOlleenJonosija)
                        .forEach(h -> asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(h));

                return;
            } else {
                int varasijat = jono.getVarasijat();
                jonoWrapper.getHakemukset().stream()
                        .filter(HakemusWrapper::isVaralla)
                        .sorted(Comparator.comparing(h -> h.getHakemus().getJonosija()))
                        .skip(varasijat)
                        .forEach(h -> asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(h));
                return;
            }
        }

        throw new IllegalStateException("Jonolla " + jono.getOid() + " piti olla ei varasijatäyttöä tai rajoitettu varasijatäyttö. Vaikuttaa bugilta.");
    }

    private void assertSijoiteltuEnnenVarasijataytonLoppumista(ValintatapajonoWrapper jonoWrapper, SijoitteluajoWrapper sijoitteluajoWrapper) {
        Valintatapajono jono = jonoWrapper.getValintatapajono();
        if (!jono.getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()
            && sijoitteluajoWrapper.onkoVarasijaSaannotVoimassaJaVarasijaTayttoPaattynyt(jonoWrapper)) {
                throw new IllegalStateException("Haun " + sijoitteluajoWrapper.getSijoitteluajo().getHakuOid() + " hakukohteen " +
                    jonoWrapper.getHakukohdeWrapper().getHakukohde().getOid() + " valintatapajonoa " + jono.getOid() +
                    " ei ole kertaakaan sijoiteltu ilman varasijasääntöjä niiden ollessa voimassa, vaikka sen varasijatäyttö " +
                    "on jo päättynyt.");
        }
    }
}
