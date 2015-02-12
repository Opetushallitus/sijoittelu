package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import fi.vm.sade.sijoittelu.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static java.util.Optional.*;
/**
 * 
 * @author Kari Kammonen
 * 
 */
public class HenkiloWrapper {

    private List<HakemusWrapper> hakemukset = new ArrayList<HakemusWrapper>();

    private String hakijaOid;

    private String hakemusOid;

    //henkilolla voi poikkeustapauksissa olla useampi valintatulos
    private List<Valintatulos> valintatulos = new ArrayList<Valintatulos>();

    public List<HakemusWrapper> getHakemukset() {
        return hakemukset;
    }

    public String getHakijaOid() {
        return hakijaOid;
    }

    public void setHakijaOid(String henkiloOid) {
        this.hakijaOid = henkiloOid;
    }

    public String getHakemusOid() {
        return hakemusOid;
    }


    public void setHakemusOid(String hakemusOid) {
        this.hakemusOid = hakemusOid;
    }

    public List<Valintatulos> getValintatulos() {
        return valintatulos;
    }

    public void setValintatulos(List<Valintatulos> valintatulos) {
        this.valintatulos = valintatulos;
    }

    public void hash(Hasher hf) {
        valintatulos.stream().forEach(v -> {
            ofNullable(v.getHakemusOid()).ifPresent(t -> hf.putUnencodedChars(t));
            // hakijaoidin tieto sisaltyy jo hakemusoidiin, ei muutoksia sijoittelunaikana, ei yksiloi enempaa jos lisataan
            //ofNullable(v.getHakijaOid()).ifPresent(t -> hf.putUnencodedChars(t));
            ofNullable(v.getHakukohdeOid()).ifPresent(t -> hf.putUnencodedChars(t));
            //v.getHakuOid(); ei tarvita hakuOid on kaikilla sama samassa haussa. ei yksiloi.
            hf.putInt(v.getHakutoive());
            hf.putBoolean(v.getHyvaksyttyVarasijalta());
            if(v.getIlmoittautumisTila() != null) {
                hf.putInt(v.getIlmoittautumisTila().ordinal());
            } else {
                // hf.putInt(-1); // jotain milla erotetaan null jostain arvosta
            }
            hf.putBoolean(v.getJulkaistavissa());
            if(v.getTila() != null) {
                hf.putInt(v.getTila().ordinal());
            } else {
                // hf.putInt(-1); // jotain milla erotetaan null jostain arvosta
            }
            ofNullable(v.getValintatapajonoOid()).ifPresent(t -> hf.putUnencodedChars(t));
        });
    }

}
