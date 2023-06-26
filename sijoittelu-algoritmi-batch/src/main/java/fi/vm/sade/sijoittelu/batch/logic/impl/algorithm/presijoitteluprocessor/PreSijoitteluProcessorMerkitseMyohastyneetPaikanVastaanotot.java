package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class PreSijoitteluProcessorMerkitseMyohastyneetPaikanVastaanotot implements PreSijoitteluProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(PreSijoitteluProcessorMerkitseMyohastyneetPaikanVastaanotot.class);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        LOG.info("Aloitetaan merkitse myohastyneet automaattisesti preprosessointi");
        sijoitteluajoWrapper.getHakukohteet().forEach(hk -> {
            hk.getValintatapajonot().forEach(jono -> {
                LOG.info("Valintatapajono {} aseta myöhästyneet automaattisesti: {}", jono.getValintatapajono().getOid(), jono.getMerkitseMyohAuto());
                if (jono.getMerkitseMyohAuto()) {
                    jono.getHakemukset().stream().filter(this::isHakemusMyohassa).forEach(this::setHakemusToStatusPerunut);
                }
            });
        });
    }

    private void setHakemusToStatusPerunut(HakemusWrapper hakemusWrapper) {
        LOG.info("Merkitään vastaanotto myöhästyneeksi hakemukselle {}, deadline: {}",
                hakemusWrapper.getHakemus().getHakemusOid(),
                hakemusWrapper.getHakemus().getVastaanottoDeadline());
        hakemusWrapper.getValintatulos().map(vt -> {
            vt.setTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, "Ei vastaan otettu määräaikana");
            return vt;
        });
        TilojenMuokkaus.asetaTilaksiPerunutEiVastaanottanutMaaraaikana(hakemusWrapper);
        hakemusWrapper.setTilaVoidaanVaihtaa(false);
    }

    private boolean isHakemusMyohassa(HakemusWrapper hakemusWrapper) {
        Hakemus hakemus = hakemusWrapper.getHakemus();
        HakemuksenTila tila = hakemus.getTila();
        boolean myohassa = List.of(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY).contains(tila)
                && hakemusWrapper.getValintatulos().map(vt -> vt.getTila() == ValintatuloksenTila.KESKEN).orElse(false)
                && hakemus.isVastaanottoMyohassa() == Boolean.TRUE;
        LOG.info("Hakemus {}, tila: {}, isVastaanottoMyohassa: {}, valintatuloksenTila: {}, myohassa: {}", hakemus.getHakemusOid(),
                tila, hakemus.isVastaanottoMyohassa(), hakemusWrapper.getValintatulos().map(Valintatulos::getTila).map(Object::toString).orElse(""), myohassa);
        return myohassa;
    }
}
