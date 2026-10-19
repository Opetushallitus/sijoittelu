package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

class PreSijoitteluProcessorJarjesteleAloituspaikatTayttojonoihin implements PreSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PreSijoitteluProcessorJarjesteleAloituspaikatTayttojonoihin.class);

    private static Set<HakemuksenTila> hyvaksyttavissaTilat = Sets.newHashSet(
            null,
            HakemuksenTila.HYVAKSYTTY,
            HakemuksenTila.VARALLA,
            HakemuksenTila.VARASIJALTA_HYVAKSYTTY);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        // Käy läpi jokaisen sijoitteluajon hakukohteen valintatapajonot
        for (HakukohdeWrapper hakukohde : sijoitteluajoWrapper.getHakukohteet()) {
            setAlkuperaisetAloituspaikat(hakukohde);

            if(sijoitteluajoWrapper.isKKHaku()) {
                Map<String, ValintatapajonoWrapper> oid2Valintatapajono = hakukohde.getValintatapajonot().stream()
                    .collect(Collectors.toUnmodifiableMap(j -> j.getValintatapajono().getOid(), Function.identity()));

                // Iteroidaan jonot läpi täyttöjonoketjun mukaisessa järjestyksessä, jotta jonolle jo
                // siirtyneet ylijäämäpaikat siirtyvät tarvittaessa edelleen ketjussa seuraavalle jonolle
                for (ValintatapajonoWrapper valintatapajonoWrapper : jarjestaTayttojonoketjunMukaan(hakukohde)) {
                    Valintatapajono valintatapajono = valintatapajonoWrapper.getValintatapajono();
                    int ylijaamaPaikat = getJaljellaOlevatAloituspaikat(valintatapajonoWrapper);

                    if (ylijaamaPaikat > 0 && StringUtils.isNotBlank(valintatapajono.getTayttojono())) {
                        siirraYlijaamaPaikatTayttojonolle(valintatapajono, ylijaamaPaikat, oid2Valintatapajono);
                    }
                }
            }
        }
    }

    /**
     * Järjestää hakukohteen valintatapajonot niin, että jono tulee aina ennen omaa täyttöjonoaan.
     * Näin ketjussa A -&gt; B -&gt; C jonosta A siirtyneet ylijäämäpaikat ehtivät siirtyä jonosta B
     * edelleen jonoon C. Mahdolliseen silmukkaan kuuluvat jonot jäävät alkuperäiseen järjestykseensä.
     */
    private List<ValintatapajonoWrapper> jarjestaTayttojonoketjunMukaan(HakukohdeWrapper hakukohde) {
        List<ValintatapajonoWrapper> jarjestamattomat = new ArrayList<>(hakukohde.getValintatapajonot());
        List<ValintatapajonoWrapper> jarjestetyt = new ArrayList<>(jarjestamattomat.size());

        while (!jarjestamattomat.isEmpty()) {
            // Seuraavaksi voidaan ottaa jono, johon mikään vielä järjestämätön jono ei osoita
            // täyttöjonollaan, koska sen aloituspaikkamäärä ei enää voi kasvaa
            ValintatapajonoWrapper seuraava = etsiJonoJohonEiOsoiteta(jarjestamattomat);
            if (seuraava == null) {
                LOG.warn("Hakukohteen {} valintatapajonojen täyttöjonoissa on silmukka (jonot {}). "
                                + "Silmukkaan kuuluvat jonot käsitellään alkuperäisessä järjestyksessään.",
                        hakukohde.getHakukohde().getOid(),
                        jarjestamattomat.stream().map(jono -> jono.getValintatapajono().getOid()).toList());
                jarjestetyt.addAll(jarjestamattomat);
                break;
            }

            jarjestamattomat.remove(seuraava);
            jarjestetyt.add(seuraava);
        }

        return jarjestetyt;
    }

    private ValintatapajonoWrapper etsiJonoJohonEiOsoiteta(List<ValintatapajonoWrapper> jonot) {
        return jonot.stream()
                .filter(jono -> !onJonkinTayttojono(jono, jonot))
                .findFirst()
                .orElse(null);
    }

    private boolean onJonkinTayttojono(ValintatapajonoWrapper jono, List<ValintatapajonoWrapper> jonot) {
        String oid = jono.getValintatapajono().getOid();
        return StringUtils.isNotBlank(oid)
                && jonot.stream().anyMatch(muu -> oid.equals(muu.getValintatapajono().getTayttojono()));
    }

    private void setAlkuperaisetAloituspaikat(HakukohdeWrapper hakukohde) {
        hakukohde.getValintatapajonot().forEach(valintatapajonoWrapper -> {
            Valintatapajono valintatapajono = valintatapajonoWrapper.getValintatapajono();
            valintatapajono.setAlkuperaisetAloituspaikat(valintatapajono.getAloituspaikat());
        });
    }

    private int getJaljellaOlevatAloituspaikat(ValintatapajonoWrapper wrapper) {
        return wrapper.getValintatapajono().getAloituspaikat() - getHyvaksyttavatHakemuksetSize(wrapper.getHakemukset());
    }

    private int getHyvaksyttavatHakemuksetSize(List<HakemusWrapper> hakemuksetWrapper) {
        return Collections2.filter(hakemuksetWrapper,
                hakemusWrapper -> hyvaksyttavissaTilat.contains(hakemusWrapper.getHakemus().getEdellinenTila())
        ).size();
    }

    private void siirraYlijaamaPaikatTayttojonolle(Valintatapajono valintatapajono,
                                                   int jaljellaOlevatAloituspaikat,
                                                   Map<String, ValintatapajonoWrapper> oid2Valintatapajono) {
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
