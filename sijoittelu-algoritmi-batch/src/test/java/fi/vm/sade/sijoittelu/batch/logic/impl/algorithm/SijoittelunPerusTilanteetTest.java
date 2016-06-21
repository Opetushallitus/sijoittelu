package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import org.junit.Test;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitusAjoBuilder.assertSijoittelu;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitusAjoBuilder.luoAlkuTilanneJaAssertSijoittelu;

public class SijoittelunPerusTilanteetTest {
    @Test
    public void alitayttoVsHakijaryhma() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(3, "A", "B", "C", "D");
        after.jono(5, Tasasijasaanto.ALITAYTTO).hyvaksytty("A","B","C","E").samaJonosija(j -> j.varalla("D", "F"));
        luoAlkuTilanneJaAssertSijoittelu(after);
    }
    @Test
    public void ylitayttoVsHakijaryhma() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(3, "A", "B", "C", "D");
        after.jono(5, Tasasijasaanto.YLITAYTTO).hyvaksytty("A","B","C","E").samaJonosija(j -> j.hyvaksytty("F", "D"));
        luoAlkuTilanneJaAssertSijoittelu(after);
    }
    @Test
    public void arvontaVsHakijaryhma() {
        SijoitusAjoBuilder dEnsin = new SijoitusAjoBuilder();
        dEnsin.hakijaryhma(3, "A", "B", "C", "D");
        dEnsin.jono(5, Tasasijasaanto.ARVONTA).hyvaksytty("A","B","C","E").samaJonosija(j -> j.hyvaksytty("D").varalla("F"));
        luoAlkuTilanneJaAssertSijoittelu(dEnsin);
        SijoitusAjoBuilder fEnsin = new SijoitusAjoBuilder();
        fEnsin.hakijaryhma(3, "A", "B", "C", "D");
        fEnsin.jono(5, Tasasijasaanto.ARVONTA).hyvaksytty("A","B","C","E").samaJonosija(j -> j.hyvaksytty("F").varalla("D"));
        luoAlkuTilanneJaAssertSijoittelu(fEnsin);
    }
    @Test
    public void peruutaToisenPaikanVastaanottanutHakijaYPS() {
        SijoitusAjoBuilder before = new SijoitusAjoBuilder().julkaiseKaikki();
        SijoitusAjoBuilder after = new SijoitusAjoBuilder().julkaiseKaikki();
        before.jono(2).varalla("A", "B", "C").vastaanottanutToisessaHaussa("A");
        after.jono(2).peruuntunut("A").hyvaksytty("B", "C");
        assertSijoittelu(before, after);
    }
    @Test
    public void sailytaAlkuperainenPerunutValintatila() {
        SijoitusAjoBuilder before = new SijoitusAjoBuilder().julkaiseKaikki();
        SijoitusAjoBuilder after = new SijoitusAjoBuilder().julkaiseKaikki();
        boolean asetaMyosEdellinenTila = true;
        before.jono(2).hylatty("A").varalla("B", "C").vastaanottanutToisessaHaussa("A");
        after.jono(2).hylatty("A").hyvaksytty("B", "C");
        assertSijoittelu(before, after);
    }
    @Test
    public void sailytaAlkuperainenHylattyValintatila() {
        SijoitusAjoBuilder before = new SijoitusAjoBuilder().julkaiseKaikki();
        SijoitusAjoBuilder after = new SijoitusAjoBuilder().julkaiseKaikki();
        before.jono(2).varalla("A", "B").hylatty("C").vastaanottanutToisessaHaussa("C");
        after.jono(2).hyvaksytty("A", "B").hylatty("C");
        assertSijoittelu(before, after);
    }
}
