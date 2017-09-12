package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.SijoitteleHakukohde.hakijaHaluaa;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.SijoitteleHakukohde.saannotSallii;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuHyvaksyttyihinTiloihin;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuVaraTiloihin;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class HyvaksyttyjenKirjanpito {
    private final List<Hakemus> hakijaryhmastaHyvaksytyt;
    private final LinkedList<Hakemus> hakijaryhmanUlkopuoleltaHyvaksytyt;
    private final LinkedList<Hakemus> hakijaryhmanUlkopuoleltaHyvaksytytJoitaVoidaanSiirtaaVaralle;
    private final LinkedList<Hakemus> hakijaryhmastaHyvaksyttavissa;

    HyvaksyttyjenKirjanpito(SijoitteluajoWrapper sijoitteluajo, Set<String> hakijaryhmaanKuuluvat, ValintatapajonoWrapper jono, HakijaryhmaWrapper hakijaryhmaWrapper) {
        this.hakijaryhmastaHyvaksytyt = new LinkedList<>();
        List<HakemusWrapper> hakijaryhmanUlkopuoleltaHyvaksytytWrappers = jono.getHakemukset().stream()
            .filter(h -> !hakijaryhmaanKuuluvat.contains(h.getHakemus().getHakemusOid()))
            .filter(h -> kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila()))
            .sorted(new HakemusWrapperComparator())
            .collect(Collectors.toList());
        this.hakijaryhmanUlkopuoleltaHyvaksytyt = hakijaryhmanUlkopuoleltaHyvaksytytWrappers.stream()
            .map(HakemusWrapper::getHakemus)
            .collect(Collectors.toCollection(LinkedList::new));
        this.hakijaryhmanUlkopuoleltaHyvaksytytJoitaVoidaanSiirtaaVaralle = hakijaryhmanUlkopuoleltaHyvaksytytWrappers
            .stream().filter(hakemusWrapper -> SijoitteleHakijaryhma.voidaanKorvata(hakemusWrapper, hakijaryhmaWrapper))
            .map(HakemusWrapper::getHakemus)
            .collect(Collectors.toCollection(LinkedList::new));
        this.hakijaryhmastaHyvaksyttavissa = jono.getHakemukset().stream()
            .filter(h -> hakijaryhmaanKuuluvat.contains(h.getHakemus().getHakemusOid()))
            .filter(h -> kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila()) ||
                (kuuluuVaraTiloihin(h.getHakemus().getTila()) &&
                    !sijoitteluajo.onkoVarasijaSaannotVoimassaJaVarasijaTayttoPaattynyt(jono) &&
                    hakijaHaluaa(h) &&
                    saannotSallii(h, sijoitteluajo)))
            .map(h -> h.getHakemus())
            .sorted(new HyvaksytytEnsinHakemusComparator())
            .collect(Collectors.toCollection(LinkedList::new));
    }

    Hakemus ensimmainenHakijaryhmastaHyvaksyttavissaOleva() {
        return hakijaryhmastaHyvaksyttavissa.getFirst();
    }

    boolean eiKetaanHyvaksyttavissaHakijaryhmasta() {
        return hakijaryhmastaHyvaksyttavissa.isEmpty();
    }

    void addHakijaryhmastaHyvaksyttavissa(Hakemus h) {
        hakijaryhmastaHyvaksyttavissa.addFirst(h);
    }

    Hakemus removeFirstHakijaryhmastaHyvaksyttavissa() {
        return hakijaryhmastaHyvaksyttavissa.removeFirst();
    }

    void merkitseHyvaksytyksiHakijaryhmasta(String hakijaryhmaOid) {
        hakijaryhmastaHyvaksytyt.forEach(h -> h.getHyvaksyttyHakijaryhmista().add(hakijaryhmaOid));
    }

    public int countHakijaryhmastaHyvaksytyt() {
        return hakijaryhmastaHyvaksytyt.size();
    }

    void addHakijaryhmastaHyvaksytyt(LinkedList<Hakemus> hyvaksytyt) {
        hakijaryhmastaHyvaksytyt.addAll(hyvaksytyt);
    }

    int hyvaksyttyjenKokonaismaara() {
        return hakijaryhmastaHyvaksytyt.size() + hakijaryhmanUlkopuoleltaHyvaksytyt.size();
    }

    public Hakemus viimeinenHakijaryhmanUlkopuoleltaHyvaksyttyJokaVoidaanSiirtaaVaralle() {
        return hakijaryhmanUlkopuoleltaHyvaksytytJoitaVoidaanSiirtaaVaralle.getLast();
    }

    boolean eiOleHakijaryhmanUlkopuoleltaHyvaksyttyjaJoitaVoidaanSiirtaaVaralle() {
        return hakijaryhmanUlkopuoleltaHyvaksytytJoitaVoidaanSiirtaaVaralle.isEmpty();
    }

    void removeLastHyvaksytyt() {
        hakijaryhmanUlkopuoleltaHyvaksytytJoitaVoidaanSiirtaaVaralle.removeLast();
        hakijaryhmanUlkopuoleltaHyvaksytyt.removeLast();
    }

    void poistaHyvaksytyistaJaHyvaksyttavista(Set<String> oidit) {
        hakijaryhmastaHyvaksyttavissa.removeIf(h -> oidit.contains(h.getHakemusOid()));
        hakijaryhmastaHyvaksytyt.removeIf(h -> oidit.contains(h.getHakemusOid()));
    }

    private static class HyvaksytytEnsinHakemusComparator implements Comparator<Hakemus> {
        HakemusComparator comparator = new HakemusComparator();

        @Override
        public int compare(Hakemus h, Hakemus hh) {
            if (kuuluuHyvaksyttyihinTiloihin(h.getTila()) && !kuuluuHyvaksyttyihinTiloihin(hh.getTila())) {
                return -1;
            }
            if (!kuuluuHyvaksyttyihinTiloihin(h.getTila()) && kuuluuHyvaksyttyihinTiloihin(hh.getTila())) {
                return 1;
            }
            return comparator.compare(h, hh);
        }
    }
}
