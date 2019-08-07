package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PreSijoitteluProcessorKiilaavatHakemuksetVaralleRajatunVarasijataytonJonoissa implements PreSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PreSijoitteluProcessorKiilaavatHakemuksetVaralleRajatunVarasijataytonJonoissa.class);
    private static final Collection<HakemuksenTila> HYLATTY_TAI_PERUUNTUNUT = Arrays.asList(HakemuksenTila.HYLATTY, HakemuksenTila.PERUUNTUNUT);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        if(!sijoitteluajoWrapper.varasijaSaannotVoimassa()) {
            return;
        }
        sijoitteluajoWrapper.getHakukohteet().forEach(hakukohdeWrapper -> {
            hakukohdeWrapper.getValintatapajonot().stream()
                .filter(v -> v.getValintatapajono().rajoitettuVarasijaTaytto())
                .forEach(this::processValintatapajononKiilaavatHakemukset);
        });
    }

    private void processValintatapajononKiilaavatHakemukset(ValintatapajonoWrapper valintatapajono) {
        String valintatapajonoOid = valintatapajono.getValintatapajono().getOid();

        if(!valintatapajono.getValintatapajono().getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()) {
            LOG.debug("Valintatapajonoa {} ei ole sijoiteltu ilman varasijasääntöjä niiden ollessa voimassa. " +
                    "Ei käsitellä kiilaavia hakemuksia...", valintatapajonoOid);
            return;
        }

        int rajaJonosija = valintatapajono.getValintatapajono().getSivssnovSijoittelunVarasijataytonRajoitus()
            .map(r -> r.jonosija)
            .orElse(Integer.MIN_VALUE);

        Predicate<Hakemus> kiilaavaJonosija = (h) -> h.getJonosija() <= rajaJonosija;
        Predicate<Hakemus> hylattyTaiPeruuntunutEdellinenTila = (h) -> HYLATTY_TAI_PERUUNTUNUT.contains(h.getEdellinenTila());

        List<String> kiilaavienOidit = valintatapajono.getHakemukset().stream()
            .filter(HakemusWrapper::isTilaVoidaanVaihtaa)
            .map(HakemusWrapper::getHakemus)
            .filter(kiilaavaJonosija)
            .filter(hylattyTaiPeruuntunutEdellinenTila).map(h -> {
                h.setEdellinenTila(HakemuksenTila.VARALLA);
                TilojenMuokkaus.asetaTilaksiVaralla(h);
                return h.getHakemusOid();
            }).collect(Collectors.toList());

        if (!kiilaavienOidit.isEmpty()) {
            LOG.info("Valintatapajonossa {} on {} kpl kiilaavia hakemuksia: {}",
                valintatapajonoOid, kiilaavienOidit.size(), String.join(",", kiilaavienOidit));
        }
    }
}
