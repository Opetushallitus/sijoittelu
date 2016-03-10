package fi.vm.sade.sijoittelu.laskenta.service.business.processors;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.external.resource.ValintaTulosServiceResource;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

public class PreSijoitteluProcessorValintaTulosServiceValinnantilojenLuku implements PreSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PreSijoitteluProcessorValintaTulosServiceValinnantilojenLuku.class);
    private final ValintaTulosServiceResource valintaTulosServiceResource;

    public PreSijoitteluProcessorValintaTulosServiceValinnantilojenLuku(ValintaTulosServiceResource valintaTulosServiceResource) {
        this.valintaTulosServiceResource = valintaTulosServiceResource;
    }

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        LOG.info("(hakuOid={}) Haetaan valinnantilat Valinta-tulos-service -palvelusta", sijoitteluajoWrapper.getSijoitteluajo().getHakuOid());
        MultiKeyMap<String, Valintatulos> vastaanottotiedot = indexValintatulokset(valintaTulosServiceResource.valintatuloksetValinnantilalla(sijoitteluajoWrapper.getSijoitteluajo().getHakuOid()));
        valintatulokset(sijoitteluajoWrapper).forEach(v -> {
            ValintatuloksenTila tila = ValintatuloksenTila.KESKEN;
            String jonoOid = v.getValintatapajonoOid();
            String hakemusOid = v.getHakemusOid();
            String hakukohdeOid = v.getHakukohdeOid();
            if (vastaanottotiedot.containsKey(jonoOid, hakemusOid, hakukohdeOid)) {
                tila = vastaanottotiedot.get(jonoOid, hakemusOid, hakukohdeOid).getTila();
                if (null == tila) {
                    throw new IllegalStateException(String.format("Hakemus: %s, hakukohde: %s, jono: %s vastaanottotieto valinta-tulos-servicestä on null", hakemusOid, hakukohdeOid, jonoOid));
                }
            }
            v.setTila(tila, "");
        });
    }

    private Stream<Valintatulos> valintatulokset(SijoitteluajoWrapper sijoitteluajoWrapper) {
        return sijoitteluajoWrapper.getHakukohteet().stream().flatMap(HakukohdeWrapper::hakukohteenHakijat).flatMap(h -> h.getValintatulos().stream());
    }

    private MultiKeyMap<String, Valintatulos> indexValintatulokset(List<Valintatulos> valintatulokset) {
        MultiKeyMap<String, Valintatulos> vs = new MultiKeyMap<>();
        valintatulokset.stream().forEach(v -> {
            String jonoOid = v.getValintatapajonoOid();
            String hakemusOid = v.getHakemusOid();
            String hakukohdeOid = v.getHakukohdeOid();
            if (vs.containsKey(jonoOid, hakemusOid, hakukohdeOid)) {
                throw new IllegalStateException(String.format("Hakemus: %s, hakukohde: %s, jono: %s useita valintatuloksia", hakemusOid, hakukohdeOid, jonoOid));
            }
            vs.put(jonoOid, hakemusOid, hakukohdeOid, v);
        });
        return vs;
    }
}
