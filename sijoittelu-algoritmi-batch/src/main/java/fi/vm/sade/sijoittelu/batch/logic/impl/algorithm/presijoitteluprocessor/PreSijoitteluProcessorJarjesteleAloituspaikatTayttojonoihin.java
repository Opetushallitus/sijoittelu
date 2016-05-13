package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import com.google.common.collect.Collections2;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluSilmukkaException;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

class PreSijoitteluProcessorJarjesteleAloituspaikatTayttojonoihin implements PreSijoitteluProcessor {

    private static final int LIMIT = 1000;

    private Map<String, ValintatapajonoWrapper> oid2valintatapajono = Maps.newHashMap();
    private Set<HakemuksenTila> hyvaksyttavissaTilat = Sets.newHashSet(
            null,
            HakemuksenTila.HYVAKSYTTY,
            HakemuksenTila.VARALLA,
            HakemuksenTila.VARASIJALTA_HYVAKSYTTY);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {

        // Hae kaikki sijoitteluajon kaikkien hakukohteiden valintatapajonot
        List<ValintatapajonoWrapper> vtjws = sijoitteluajoWrapper
                .getHakukohteet().stream()
                .flatMap(s -> s.getValintatapajonot().stream())
                .collect(Collectors.toList());

        // Populoi oid2valintatapajono map
        vtjws.forEach(vtj -> oid2valintatapajono.put(vtj.getValintatapajono().getOid(), vtj));

        Queue<ValintatapajonoWrapper> toBeProcessed = Queues.newConcurrentLinkedQueue(vtjws);

        // Iteroidaan jokaisen jonon ja täyttöjonojen läpi
        int iterationCount = 0;
        while (!toBeProcessed.isEmpty()) {
            if(iterationCount++ > LIMIT)
                throw new SijoitteluSilmukkaException();

            ValintatapajonoWrapper vtjw = toBeProcessed.poll();

            List<HakemusWrapper> hakemusWrappers = vtjw.getHakemukset();
            Valintatapajono valintatapajono = vtjw.getValintatapajono();

            // Laske hakemuksista kaikki HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY ja VARALLA
            int jonossaHyvaksyttavissa = Collections2.filter(hakemusWrappers,
                    hakemusWrapper -> hyvaksyttavissaTilat.contains(hakemusWrapper.getHakemus().getEdellinenTila())
            ).size();

            int jaljellaolevatAloituspaikat = valintatapajono.getAloituspaikat() - jonossaHyvaksyttavissa;
            if (jaljellaolevatAloituspaikat > 0 && StringUtils.isNotBlank(valintatapajono.getTayttojono())) {

                // Sirrä ylijäämä aloituspaikat täyttöjonolle
                ValintatapajonoWrapper tayttojonoWrapper = oid2valintatapajono.get(valintatapajono.getTayttojono());
                Valintatapajono tayttojono = tayttojonoWrapper.getValintatapajono();
                tayttojono.setAloituspaikat(tayttojono.getAloituspaikat() + jaljellaolevatAloituspaikat);
                valintatapajono.setAloituspaikat(valintatapajono.getAloituspaikat() - jaljellaolevatAloituspaikat);

                // Tarkistetaan täyttöjono vielä uudestaan.
                toBeProcessed.add(tayttojonoWrapper);
            }
        }

    }


}
