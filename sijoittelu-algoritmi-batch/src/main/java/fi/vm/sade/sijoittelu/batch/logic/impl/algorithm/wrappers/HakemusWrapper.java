package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import com.google.common.hash.Hasher;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Pistetieto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

import static java.util.Optional.ofNullable;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class HakemusWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(HakemusWrapper.class);
    private Hakemus hakemus;

    private ValintatapajonoWrapper valintatapajono;

    private HenkiloWrapper henkilo;

    // Yhden hakukohderekursion aikainen lippu, jolla katsotaan voidaanko korvata
    private boolean hyvaksyttyHakijaryhmasta = false;

    // Yhden hakukohderekursion aikainen lippu, jolla katsotaan voidaanko korvata
    private boolean hyvaksyttyValintatapaJonosta = false;

    //jos hakemuksen tilaa ei voida muuttaa, esm. ilmoitettu hakijalle jo
    private boolean tilaVoidaanVaihtaa = true;

    public HenkiloWrapper getHenkilo() {
        return henkilo;
    }

    public void setHenkilo(HenkiloWrapper henkilo) {
        this.henkilo = henkilo;
    }

    public Hakemus getHakemus() {
        return hakemus;
    }

    public void setHakemus(Hakemus hakemus) {
        this.hakemus = hakemus;
    }

    public ValintatapajonoWrapper getValintatapajono() {
        return valintatapajono;
    }

    public void setValintatapajono(ValintatapajonoWrapper valintatapajono) {
        this.valintatapajono = valintatapajono;
    }


    public boolean isTilaVoidaanVaihtaa() {
        return tilaVoidaanVaihtaa;
    }

    public void setTilaVoidaanVaihtaa(boolean tilaVoidaanVaihtaa) {
        this.tilaVoidaanVaihtaa = tilaVoidaanVaihtaa;
    }

    public boolean isHyvaksyttyHakijaryhmasta() {
        return hyvaksyttyHakijaryhmasta;
    }

    public void setHyvaksyttyHakijaryhmasta(boolean hyvaksyttyHakijaryhmasta) {
        this.hyvaksyttyHakijaryhmasta = hyvaksyttyHakijaryhmasta;
    }

    public void hash(Hasher hf) {
        if(hakemus != null) {
            ofNullable(hakemus.getEdellinenTila()).ifPresent(t -> hf.putInt(t.ordinal()));
            //hakemus.getEtunimi(); // ei yksiloi ja yksiloiva tieto saadaan jo hakemusoidista
            //hakemus.getHakijaOid(); // sama yksiloiva tieto jo hakemusoidissa
            ofNullable(hakemus.getHakemusOid()).ifPresent(t -> hf.putUnencodedChars(t));
            ofNullable(hakemus.getIlmoittautumisTila()).ifPresent(t -> hf.putInt(t.ordinal()));
            ofNullable(hakemus.getJonosija()).ifPresent(t -> hf.putInt(t));
            ofNullable(hakemus.getPisteet()).ifPresent(t -> hf.putUnencodedChars(t.toString()));
            ofNullable(hakemus.getPistetiedot()).orElse(Collections.<Pistetieto>emptyList()).forEach(p -> {
                ofNullable(p.getArvo()).ifPresent(a -> hf.putUnencodedChars(a));
                ofNullable(p.getLaskennallinenArvo()).ifPresent(a -> hf.putUnencodedChars(a));
                ofNullable(p.getOsallistuminen()).ifPresent(a -> hf.putUnencodedChars(a));
                ofNullable(p.getTunniste()).ifPresent(a -> hf.putUnencodedChars(a));
            });
            ofNullable(hakemus.getPrioriteetti()).ifPresent(t -> hf.putInt(t));
            //hakemus.getSukunimi();
            ofNullable(hakemus.getTasasijaJonosija()).ifPresent(t -> hf.putInt(t));
            ofNullable(hakemus.getTila()).ifPresent(t -> hf.putInt(t.ordinal()));
            //hakemus.getTilaHistoria(); onko tilahistorialla merkitystä?
            //hakemus.getTilanKuvaukset();
            ofNullable(hakemus.getVarasijanNumero()).ifPresent(t -> hf.putInt(t));
        } else {
            LOG.error("Hakemuswrapperilla ei ole hakemusta!");
            throw new RuntimeException("Hakemuswrapperilla ei ole hakemusta!");
        }
    }

    public boolean isHyvaksyttyValintatapaJonosta() {
        return hyvaksyttyValintatapaJonosta;
    }

    public void setHyvaksyttyValintatapaJonosta(boolean hyvaksyttyValintatapaJonosta) {
        this.hyvaksyttyValintatapaJonosta = hyvaksyttyValintatapaJonosta;
    }
}
