package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hash;

import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import junit.framework.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

/**
 * @author Jussi Jartamo
 */
public class SijoitteluajoWrapperHashTest {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluajoWrapperHashTest.class);

    @Test
    public void testaaSijoitteluAjonTilanHash() {
        SijoitteluajoWrapper ajo = createSijoitteluAjoHashTestiin();
        Hakemus hakemus = ajo.getHakukohteet().iterator().next().getValintatapajonot().iterator().next()
                .getHakemukset().iterator().next().getHakemus();
        Valintatulos valintatulos1 = ajo.getHakukohteet().iterator().next()
                .hakukohteenHakijat().findFirst().get().getValintatulos().iterator().next();

        for (int i = 0; i < 10; ++i) {
            long t0 = System.currentTimeMillis();
            HashCode hcode;
            if (i <= 5) {
                hakemus.setJonosija(hakemus.getJonosija() + 1);
            } else {
                valintatulos1.setHakutoive(valintatulos1.getHakutoive() + 1);
            }
            hcode = ajo.asHash();

            long t1 = (System.currentTimeMillis() - t0);
            LOG.info("SijoitteluAjoWrapperin HASH on {}, laskenta kesti {}ms", hcode, t1);
        }
    }
    @Test
    public void testaaEttaNullEnumMuuttaaHashArvoa() {
        SijoitteluajoWrapper ajo = createSijoitteluAjoHashTestiin();
        Hakemus hakemus = ajo.getHakukohteet().iterator().next().getValintatapajonot().iterator().next()
                .getHakemukset().iterator().next().getHakemus();
        Valintatulos valintatulos1 = ajo.getHakukohteet().iterator().next()
                .hakukohteenHakijat().findFirst().get().getValintatulos().iterator().next();


        valintatulos1.setTila(null);
        HashCode h0 = ajo.asHash();

        Set<HashCode> hashes = Sets.newHashSet();
        for(ValintatuloksenTila v0 : ValintatuloksenTila.values()) {
            valintatulos1.setTila(v0);
            hashes.add(ajo.asHash());
        }
        Assert.assertFalse(hashes.contains(h0));

    }

    private SijoitteluajoWrapper createSijoitteluAjoHashTestiin() {
        SijoitteluajoWrapper ajo = new SijoitteluajoWrapper();
        HenkiloWrapper henkilo1 = new HenkiloWrapper();
        HakemusWrapper hakemus1 = new HakemusWrapper();
        Hakemus hakemus = new Hakemus();
        hakemus.setJonosija(1);
        hakemus1.setHakemus(hakemus);
        hakemus1.setHenkilo(henkilo1);
        Valintatulos valintatulos1 = new Valintatulos();
        henkilo1.getHakemukset().add(hakemus1);
        henkilo1.getValintatulos().add(valintatulos1);

        HakijaryhmaWrapper hakijaryhma = new HakijaryhmaWrapper();
        hakijaryhma.getHenkiloWrappers().add(henkilo1);
        ValintatapajonoWrapper valintatapajonoWrapper = new ValintatapajonoWrapper();
        valintatapajonoWrapper.getHakemukset().add(hakemus1);
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid(UUID.randomUUID().toString());
        HakukohdeWrapper hakukohdeWrapper = new HakukohdeWrapper();
        hakukohdeWrapper.setHakukohde(hakukohde);
        hakukohdeWrapper.getHakijaryhmaWrappers().add(hakijaryhma);
        hakukohdeWrapper.getValintatapajonot().add(valintatapajonoWrapper);
        ajo.getHakukohteet().add(hakukohdeWrapper);
        return ajo;
    }

}
