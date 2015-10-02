package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import org.junit.Ignore;
import org.junit.Test;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitusAjoBuilder.assertSijoittelu;

@Ignore
public class EnsikertalaisuusKiintionKayttoTest {

    @Test
    public void ilmanHakijaryhmaa() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.jono(2).hyvaksytty("A", "C").varalla("E", "H", "B", "F", "D");
        after.jono(2).peruuntunut("C").hyvaksytty("H", "B").varalla("F").peruuntunut("A").varalla("D", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim1() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(2).hyvaksytty("A", "C").varalla("E", "H", "B", "F", "D");
        after.jono(2).peruuntunut("C").hyvaksytty("H", "B").varalla("F").peruuntunut("A").varalla("D", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim1B() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.jono(2).hakijaryhma(1, "C", "B", "D").hyvaksytty("A", "C").varalla("E", "H", "B", "F", "D");
        after.jono(2).hakijaryhma(1, "C", "B", "D").peruuntunut("C").hyvaksytty("H", "B").varalla("F").peruuntunut("A").varalla("D", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim1C() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(1).hyvaksytty("C").varalla("B", "D");
        after.jono(1).peruuntunut("C").hyvaksytty("B").varalla("D");
        after.jono(1).hyvaksytty("A").peruuntunut("C").varalla("E", "H").peruuntunut("B").varalla("F", "D");
        after.jono(1).peruuntunut("C").hyvaksytty("H").peruuntunut("B").varalla("F").peruuntunut("A").varalla("D", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim1_1() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(2).hyvaksytty("A").perunut("C").varalla("E", "H").hyvaksytty("B").varalla("F", "D");
        after.jono(2).perunut("C").hyvaksytty("H").peruuntunut("B").varalla("F").peruuntunut("A").hyvaksytty("D").varalla("E");
        assertSijoittelu(after);
    }

    @Test
    public void esim1_2() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(2).hyvaksytty("A").perunut("C").varalla("E", "H").perunut("B").varalla("F").hyvaksytty("D");
        after.jono(2).perunut("C").hyvaksytty("H").perunut("B").hyvaksytty("F").peruuntunut("A").peruuntunut("D").varalla("E");
        assertSijoittelu(after);
    }

    @Test
    public void esim2() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(2).hyvaksytty("A", "E").varalla("H", "F", "B", "D", "C");
        after.jono(2).varalla("H", "F").hyvaksytty("C", "B").peruuntunut("A").varalla("D").peruuntunut("E");
        assertSijoittelu(after);
    }

    @Test
    public void esim2B() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.jono(2).hakijaryhma(1, "C", "B", "D").hyvaksytty("A").varalla("E", "H", "F").hyvaksytty("B").varalla("D", "C");
        after.jono(2).hakijaryhma(1, "C", "B", "D").hyvaksytty("H").varalla("F").hyvaksytty("C").peruuntunut("B", "A").varalla("D", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim2C() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(1).hyvaksytty("B").varalla("D", "C");
        after.jono(1).hyvaksytty("C").peruuntunut("B").varalla("D");
        after.jono(1).hyvaksytty("A").varalla("E", "H", "F").peruuntunut("B").varalla("D").peruuntunut("C");
        after.jono(1).peruuntunut("C").hyvaksytty("H").peruuntunut("B").varalla("F").peruuntunut("A").varalla("D", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim3() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(2, Tasasijasaanto.ALITAYTTO).samaJonosija(j -> j.varalla("A").hyvaksytty("C").varalla("E")).varalla("H", "B", "F", "D");
        after.jono(2, Tasasijasaanto.ALITAYTTO).samaJonosija(j -> j.hyvaksytty("D").varalla("H").hyvaksytty("B")).varalla("F", "A").peruuntunut("C").varalla("E");
        assertSijoittelu(after);
    }

    @Test
    public void esim3B() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.jono(2, Tasasijasaanto.ALITAYTTO).hakijaryhma(1, "C", "B", "D").samaJonosija(j -> j.varalla("A").hyvaksytty("C").varalla("E")).varalla("H", "B", "F", "D");
        after.jono(2, Tasasijasaanto.ALITAYTTO).hakijaryhma(1, "C", "B", "D").samaJonosija(j -> j.hyvaksytty("D").varalla("H").hyvaksytty("B")).varalla("F", "A").peruuntunut("C").varalla("E");
        assertSijoittelu(after);
    }

    @Test
    public void esim3C() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(1, Tasasijasaanto.ALITAYTTO).hyvaksytty("C").varalla("B", "D");
        after.jono(1, Tasasijasaanto.ALITAYTTO).samaJonosija(j -> j.varalla("D", "B")).peruuntunut("C");
        after.jono(1, Tasasijasaanto.ALITAYTTO).samaJonosija(j -> j.varalla("A").peruuntunut("C").varalla("E")).varalla("H", "B", "F", "D");
        after.jono(1, Tasasijasaanto.ALITAYTTO).samaJonosija(j -> j.varalla("D", "H", "B")).varalla("F", "A").peruuntunut("C").varalla("E");
        assertSijoittelu(after);
    }

    @Test
    public void esim4() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("A", "C", "E")).varalla("H", "B", "F", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("D", "H", "B")).varalla("F").peruuntunut("A", "C", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim4B() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.jono(2, Tasasijasaanto.YLITAYTTO).hakijaryhma(1, "C", "B", "D").samaJonosija(j -> j.hyvaksytty("A", "C", "E")).varalla("H", "B", "F", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).hakijaryhma(1, "C", "B", "D").samaJonosija(j -> j.hyvaksytty("D", "H", "B")).varalla("F").peruuntunut("A", "C", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim4C() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(1, Tasasijasaanto.YLITAYTTO).hyvaksytty("C").varalla("B", "D");
        after.jono(1, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("D", "B")).peruuntunut("C");
        after.jono(1, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("A").peruuntunut("C").hyvaksytty("E")).varalla("H").peruuntunut("B").varalla("F").peruuntunut("D");
        after.jono(1, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.peruuntunut("D").hyvaksytty("H").peruuntunut("B")).varalla("F").peruuntunut("A", "C", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim4_1_vanha() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("A").perunut("C").hyvaksytty("E")).varalla("H", "B", "F", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("D", "H", "B")).varalla("F").peruuntunut("A").perunut("C").peruuntunut("E");
        assertSijoittelu(after);
    }

    @Test
    public void esim4_1() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("A").perunut("C","E")).varalla("H").hyvaksytty("B").varalla("F", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("D", "H").peruuntunut("B")).varalla("F").peruuntunut("A", "C", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim4_1B_vanha() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("A").perunut("C").hyvaksytty("E")).varalla("H").perunut("B").varalla("F", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("D", "H").perunut("B")).varalla("F").peruuntunut("A").perunut("C").peruuntunut("E");
        assertSijoittelu(after);
    }

    @Test
    public void esim5() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("A", "H", "E")).varalla("C", "B", "F", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("D").peruuntunut("H").hyvaksytty("B")).varalla("F").peruuntunut("A").varalla("C").peruuntunut("E");
        assertSijoittelu(after);
    }

    @Test
    public void esim5B() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.jono(2, Tasasijasaanto.YLITAYTTO).hakijaryhma(1, "C", "B", "D").samaJonosija(j -> j.hyvaksytty("A", "H", "E")).hyvaksytty("C").varalla("B", "F", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).hakijaryhma(1, "C", "B", "D").samaJonosija(j -> j.hyvaksytty("D").peruuntunut("H").hyvaksytty("B")).varalla("F").peruuntunut("A", "C", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim5C() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(1, Tasasijasaanto.YLITAYTTO).hyvaksytty("C").varalla("B", "D");
        after.jono(1, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("D", "B")).peruuntunut("C");
        after.jono(1, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("A", "H", "E")).peruuntunut("C", "B").varalla("F").peruuntunut("D");
        after.jono(1, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.peruuntunut("D", "H", "B")).hyvaksytty("F").peruuntunut("A", "C", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim6() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(2, "C", "B", "D");
        after.jono(2, Tasasijasaanto.ALITAYTTO).samaJonosija(j -> j.varalla("D", "C", "B")).varalla("F", "A", "H", "E");
        assertSijoittelu(after);
    }

    @Test
    public void esim7() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(1, "C", "B", "D").hakijaryhma(1, "B");
        after.jono(2, Tasasijasaanto.ALITAYTTO).samaJonosija(j -> j.varalla("D", "H").hyvaksytty("B")).varalla("F", "A", "C", "E");
        assertSijoittelu(after);
    }

    @Test
    public void ylitayttoCase1() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(1, "B", "D");
        after.jono(2, Tasasijasaanto.YLITAYTTO).samaJonosija(j -> j.hyvaksytty("A").perunut("B").hyvaksytty("C")).hyvaksytty("D").varalla("E");
        assertSijoittelu(after);
    }

    @Test
    public void ylitayttoCase2() {
        SijoitusAjoBuilder after = new SijoitusAjoBuilder();
        after.hakijaryhma(1, "A", "D").hakijaryhma(1, "D");
        after.jono(1, Tasasijasaanto.ALITAYTTO).samaJonosija(j -> j.varalla("A", "B", "C").hyvaksytty("D"));
        assertSijoittelu(after);
    }
}