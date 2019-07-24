package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluSilmukkaException;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

class PreSijoitteluProcessorJarjesteleAloituspaikatTayttojonoihin implements PreSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PreSijoitteluProcessorJarjesteleAloituspaikatTayttojonoihin.class);
    private static final int LIMIT = 1000;

    private Map<String, ValintatapajonoWrapper> oid2Valintatapajono;
    private Set<HakemuksenTila> hyvaksyttavissaTilat = Sets.newHashSet(
            null,
            HakemuksenTila.HYVAKSYTTY,
            HakemuksenTila.VARALLA,
            HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
    private Queue<ValintatapajonoWrapper> toBeProcessed;

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        // Käy läpi jokaisen sijoitteluajon hakukohteen valintatapajonot
        for (HakukohdeWrapper hakukohde : sijoitteluajoWrapper.getHakukohteet()) {
            setAlkuperaisetAloituspaikat(hakukohde);

            if(sijoitteluajoWrapper.isKKHaku()) {
                populateOid2Valintatapajono(hakukohde);

                toBeProcessed = Queues.newConcurrentLinkedQueue(hakukohde.getValintatapajonot());

                // Iteroidaan jokaisen jonon ja täyttöjonojen läpi
                int iterationCount = 0;
                while (!toBeProcessed.isEmpty()) {
                    if (iterationCount++ > LIMIT) {
                        throw new SijoitteluSilmukkaException(
                                String.format(
                                        "Täyttöjono loop detected for sijoitteluajo %s, hakukohde %s with %s valintajonos",
                                        sijoitteluajoWrapper.getSijoitteluAjoId(),
                                        hakukohde.getHakukohde().getOid(),
                                        hakukohde.getValintatapajonot().size()));
                    }

                    ValintatapajonoWrapper valintatapajonoWrapper = toBeProcessed.poll();
                    Valintatapajono valintatapajono = valintatapajonoWrapper.getValintatapajono();
                    int ylijaamaPaikat = getJaljellaOlevatAloituspaikat(valintatapajonoWrapper);

                    if (ylijaamaPaikat > 0 && StringUtils.isNotBlank(valintatapajono.getTayttojono())) {
                        siirraYlijaamaPaikatTayttojonolle(valintatapajono, ylijaamaPaikat);
                    }
                }
            }
        }
    }

    private void setAlkuperaisetAloituspaikat(HakukohdeWrapper hakukohde) {
        hakukohde.getValintatapajonot().forEach(valintatapajonoWrapper -> {
            Valintatapajono valintatapajono = valintatapajonoWrapper.getValintatapajono();
            valintatapajono.setAlkuperaisetAloituspaikat(valintatapajono.getAloituspaikat());
        });
    }

    private void populateOid2Valintatapajono(HakukohdeWrapper hakukohde) {
        oid2Valintatapajono = Maps.newHashMap();
        hakukohde.getValintatapajonot().forEach(valintatapajonoWrapper ->
            oid2Valintatapajono.put(valintatapajonoWrapper.getValintatapajono().getOid(), valintatapajonoWrapper)
        );
    }

    private int getJaljellaOlevatAloituspaikat(ValintatapajonoWrapper wrapper) {
        return wrapper.getValintatapajono().getAloituspaikat() - getHyvaksyttavatHakemuksetSize(wrapper.getHakemukset());
    }

    private int getHyvaksyttavatHakemuksetSize(List<HakemusWrapper> hakemuksetWrapper) {
        return Collections2.filter(hakemuksetWrapper,
                hakemusWrapper -> hyvaksyttavissaTilat.contains(hakemusWrapper.getHakemus().getEdellinenTila())
        ).size();
    }

    private void siirraYlijaamaPaikatTayttojonolle(Valintatapajono valintatapajono, int jaljellaOlevatAloituspaikat) {
        ValintatapajonoWrapper valintatapajonoWrapper = oid2Valintatapajono.get(valintatapajono.getTayttojono());
        if(valintatapajonoWrapper != null) {
            Valintatapajono tayttojono = oid2Valintatapajono.get(valintatapajono.getTayttojono()).getValintatapajono();
            tayttojono.setAloituspaikat(tayttojono.getAloituspaikat() + jaljellaOlevatAloituspaikat);
            valintatapajono.setAloituspaikat(valintatapajono.getAloituspaikat() - jaljellaOlevatAloituspaikat);
        } else {
            LOG.warn("Valintatapajonon {} (OID: {}) täyttöjonoa (OID: {}) ei löytynyt.", valintatapajono.getNimi(), valintatapajono.getOid(), valintatapajono.getTayttojono());
        }
    }
}
