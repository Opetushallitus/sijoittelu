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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

class PreSijoitteluProcessorJarjesteleAloituspaikatTayttojonoihin implements PreSijoitteluProcessor {

    private static final int LIMIT = 1000;

    private Map<String, ValintatapajonoWrapper> oid2Valintatapajono;
    private Set<HakemuksenTila> hyvaksyttavissaTilat = Sets.newHashSet(
            null,
            HakemuksenTila.HYVAKSYTTY,
            HakemuksenTila.VARALLA,
            HakemuksenTila.VARASIJALTA_HYVAKSYTTY);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {

        // Käy läpi jokaisen sijoitteluajon hakukohteen valintatapajonot
        for (HakukohdeWrapper hakukohde : sijoitteluajoWrapper.getHakukohteet()) {

            setAlkuperaisetAloituspaikat(hakukohde);
            populateOid2Valintatapajono(hakukohde);

            Queue<ValintatapajonoWrapper> toBeProcessed = Queues.newConcurrentLinkedQueue(hakukohde.getValintatapajonot());

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

                List<HakemusWrapper> hakemusWrappers = valintatapajonoWrapper.getHakemukset();
                Valintatapajono valintatapajono = valintatapajonoWrapper.getValintatapajono();

                // Laske hakemuksista kaikki HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY ja VARALLA
                int jonossaHyvaksyttavissa = Collections2.filter(hakemusWrappers,
                        hakemusWrapper -> hyvaksyttavissaTilat.contains(hakemusWrapper.getHakemus().getEdellinenTila())
                ).size();

                int jaljellaolevatAloituspaikat = valintatapajono.getAloituspaikat() - jonossaHyvaksyttavissa;
                if (jaljellaolevatAloituspaikat > 0 && StringUtils.isNotBlank(valintatapajono.getTayttojono())) {

                    // Sirrä ylijäämä aloituspaikat täyttöjonolle
                    ValintatapajonoWrapper tayttojonoWrapper = oid2Valintatapajono.get(valintatapajono.getTayttojono());
                    Valintatapajono tayttojono = tayttojonoWrapper.getValintatapajono();
                    tayttojono.setAloituspaikat(tayttojono.getAloituspaikat() + jaljellaolevatAloituspaikat);
                    valintatapajono.setAloituspaikat(valintatapajono.getAloituspaikat() - jaljellaolevatAloituspaikat);

                    // Tarkistetaan täyttöjono vielä uudestaan.
                    toBeProcessed.add(tayttojonoWrapper);
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

}
