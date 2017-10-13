package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class PreSijoitteluProcessorKiilaavatHakemuksetVaralleRajatunVarasijataytonJonoissa implements PreSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PreSijoitteluProcessorKiilaavatHakemuksetVaralleRajatunVarasijataytonJonoissa.class);

    private final Collection<HakemuksenTila> HYLATTY_TAI_PERUUNTUNUT = Arrays.asList(HakemuksenTila.HYLATTY, HakemuksenTila.PERUUNTUNUT);

    private Function<ValintatapajonoWrapper, Optional<Integer>> viimeisenVarallaolijanJonosija = (valintatapajono) ->
            valintatapajono.getHakemukset().stream().map(HakemusWrapper::getHakemus)
              .filter(h -> HakemuksenTila.VARALLA.equals(h.getEdellinenTila())).map(h -> h.getJonosija()).max(Comparator.naturalOrder());

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        if(!sijoitteluajoWrapper.varasijaSaannotVoimassa()) {
            return;
        }
        sijoitteluajoWrapper.getHakukohteet().forEach(hakukohdeWrapper -> {
            hakukohdeWrapper.getValintatapajonot().stream().filter(v -> v.getValintatapajono().rajoitettuVarasijaTaytto()).forEach(valintatapajono -> {
                processValintatapajononKiilaavatHakemukset(valintatapajono);
            });
        });
    }

    private void processValintatapajononKiilaavatHakemukset(ValintatapajonoWrapper valintatapajono) {
        String valintatapajonoOid = valintatapajono.getValintatapajono().getOid();

        if(!valintatapajono.getValintatapajono().getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()) {
            LOG.debug("Valintatapajonoa {} ei ole sijoiteltu ilman varasijasääntöjä niiden ollessa voimassa. " +
                    "Ei käsitellä kiilaavia hakemuksia...", valintatapajonoOid);
            return;
        }

        List<String> kiilaavatHakemukset = new ArrayList<>();

        int viimeisimmanVarallaolijanJonosija = viimeisenVarallaolijanJonosija.apply(valintatapajono).orElse(Integer.MIN_VALUE);

        Predicate<Hakemus> kiilaavaJonosija = (h) -> h.getJonosija() <= viimeisimmanVarallaolijanJonosija;
        Predicate<Hakemus> hylattyTaiPeruuntunutEdellinenTila = (h) -> HYLATTY_TAI_PERUUNTUNUT.contains(h.getEdellinenTila());

        valintatapajono.getHakemukset().stream().map(HakemusWrapper::getHakemus)
                .filter(kiilaavaJonosija)
                .filter(hylattyTaiPeruuntunutEdellinenTila).forEach((h) -> {
            h.setEdellinenTila(HakemuksenTila.VARALLA);
            h.setTila(HakemuksenTila.VARALLA);
            kiilaavatHakemukset.add(h.getHakemusOid());
        });

        if(!kiilaavatHakemukset.isEmpty()) {
            LOG.info("Valintatapajonossa {} on {} kpl kiilaavia hakemuksia: {}",
                    valintatapajonoOid, kiilaavatHakemukset.size(), String.join(",", kiilaavatHakemukset));
        }
    }
}