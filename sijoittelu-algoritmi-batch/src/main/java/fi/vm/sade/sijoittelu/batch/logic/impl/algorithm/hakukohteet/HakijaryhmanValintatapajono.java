package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

class HakijaryhmanValintatapajono {
    final HyvaksyttyjenKirjanpito kirjanpito;
    final Tasasijasaanto tasasijasaanto;
    private final int aloituspaikkoja;
    final int prioriteetti;

    HakijaryhmanValintatapajono(SijoitteluajoWrapper sijoitteluajo, Set<String> hakijaryhmaanKuuluvat, ValintatapajonoWrapper jono, HakijaryhmaWrapper hakijaryhmaWrapper) {
        kirjanpito = new HyvaksyttyjenKirjanpito(sijoitteluajo, hakijaryhmaanKuuluvat, jono, hakijaryhmaWrapper);
        this.tasasijasaanto = jono.getValintatapajono().getTasasijasaanto();
        if (jono.getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan() != null &&
                jono.getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan()) {
            this.aloituspaikkoja = Integer.MAX_VALUE;
        } else {
            this.aloituspaikkoja = jono.getValintatapajono().getAloituspaikat();
        }
        this.prioriteetti = jono.getValintatapajono().getPrioriteetti();
    }

    List<Hakemus> hyvaksyAloituspaikkoihinJaKiintioonMahtuvatParhaallaJonosijallaOlevat(int kiintiotaJaljella) {
        int aloituspaikkoihinMahtuu = aloituspaikkoja - kirjanpito.hyvaksyttyjenKokonaismaaraMiinusKorvattavatPoissaolijat();
        int paikkoja = tasasijasaanto == Tasasijasaanto.ALITAYTTO ? aloituspaikkoihinMahtuu : Math.min(aloituspaikkoihinMahtuu, kiintiotaJaljella);
        if (kirjanpito.eiKetaanHyvaksyttavissaHakijaryhmasta() || paikkoja <= 0) {
            return Collections.emptyList();
        }
        LinkedList<Hakemus> tasasijalla = new LinkedList<>();
        do { tasasijalla.addLast(kirjanpito.removeFirstHakijaryhmastaHyvaksyttavissa()); }
        while (!kirjanpito.eiKetaanHyvaksyttavissaHakijaryhmasta() &&
            Objects.equals(tasasijalla.getLast().getJonosija(), kirjanpito.ensimmainenHakijaryhmastaHyvaksyttavissaOleva().getJonosija()));
        LinkedList<Hakemus> hyvaksytyt = new LinkedList<>();
        LinkedList<Hakemus> eiHyvaksytyt = new LinkedList<>();
        switch (tasasijasaanto) {
            case ARVONTA:
                hyvaksytyt.addAll(tasasijalla.subList(0, Math.min(paikkoja, tasasijalla.size())));
                eiHyvaksytyt.addAll(tasasijalla.subList(Math.min(paikkoja, tasasijalla.size()), tasasijalla.size()));
                break;
            case ALITAYTTO:
                if (tasasijalla.size() <= paikkoja) {
                    hyvaksytyt.addAll(tasasijalla);
                } else {
                    eiHyvaksytyt.addAll(tasasijalla);
                }
                break;
            case YLITAYTTO:
                hyvaksytyt.addAll(tasasijalla);
                break;
        }
        kirjanpito.addHakijaryhmastaHyvaksytyt(hyvaksytyt);
        eiHyvaksytyt.descendingIterator().forEachRemaining(kirjanpito::addHakijaryhmastaHyvaksyttavissa);
        return hyvaksytyt;
    }

    boolean siirraVaralleAlimmallaJonosijallaOlevatHakijaryhmanUlkopuolisetHyvaksytyt() {
        if (kirjanpito.eiOleHakijaryhmanUlkopuoleltaHyvaksyttyjaJoitaVoidaanSiirtaaVaralle()) {
            return false;
        }
        int jonosija = kirjanpito.viimeinenHakijaryhmanUlkopuoleltaHyvaksyttyJokaVoidaanSiirtaaVaralle().getJonosija();
        do {
            kirjanpito.removeLastHyvaksytyt();
        } while (!kirjanpito.eiOleHakijaryhmanUlkopuoleltaHyvaksyttyjaJoitaVoidaanSiirtaaVaralle() &&
                kirjanpito.viimeinenHakijaryhmanUlkopuoleltaHyvaksyttyJokaVoidaanSiirtaaVaralle().getJonosija() == jonosija);
        return true;
    }

}
