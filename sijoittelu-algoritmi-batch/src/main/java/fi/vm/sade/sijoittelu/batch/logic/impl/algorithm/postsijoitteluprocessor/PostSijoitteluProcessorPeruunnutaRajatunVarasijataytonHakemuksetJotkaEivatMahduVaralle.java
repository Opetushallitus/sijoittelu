package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteleHakukohde.hakijaKasiteltavienVarasijojenSisalla;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiPeruuntunutAloituspaikatTaynna;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

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
        if (jono.vapaaVarasijataytto() || jono.getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()) {
            return;
        }
        SijoitteluajoWrapper sijoitteluajoWrapper = jonoWrapper.getHakukohdeWrapper().getSijoitteluajoWrapper();
        assertSijoiteltuEnnenVarasijataytonLoppumista(jonoWrapper, sijoitteluajoWrapper);

        // TODO :Tarvitaanko?
        if (jono.getKaikkiEhdonTayttavatHyvaksytaan()) {
            return;
        }

        if (jono.getEiVarasijatayttoa()) {
            jonoWrapper.getHakemukset().forEach(h -> {
                if (h.isVaralla() /* && sijoitteluajoWrapper.isKKHaku() // TODO tarvitaanko? */ && h.isTilaVoidaanVaihtaa()) {
                    asetaTilaksiPeruuntunutAloituspaikatTaynna(h);
                }
            });
            return;
        }

        Assert.isTrue(jono.rajoitettuVarasijaTaytto(), "Jonolla " + jono.getOid()
            + " piti olla rajoitettu varasijatäyttö. Vaikuttaa bugilta.");
        int varasijat = jono.getVarasijat();
        jonoWrapper.getHakemukset().forEach(h -> {
            if (h.isVaralla() && h.isTilaVoidaanVaihtaa() && !hakijaKasiteltavienVarasijojenSisalla(h, varasijat)) {
                asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(h);
            }
        });
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