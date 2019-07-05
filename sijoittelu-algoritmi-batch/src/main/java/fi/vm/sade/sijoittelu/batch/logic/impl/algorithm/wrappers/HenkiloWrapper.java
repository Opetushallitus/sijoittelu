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
import java.util.function.Supplier;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static java.util.Optional.*;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper.ifPresentOrIfNotPresent;

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

    private final String VALUE_DELIMETER_VALINTATULOS = "_VALINTATULOS_";
    private final String VALUE_DELIMETER_HAKEMUSOID = "_HAKEMUSOID_";
    private final String VALUE_DELIMETER_HAKUKOHDE = "_HAKUKOHDE_";
    private final String VALUE_DELIMETER_HAKUTOIVE = "_HAKUTOIVE_";
    private final String VALUE_DELIMETER_HYVAKSYTTY_VARASIJALTA = "_HYVAKSYTTY_VARASIJALTA_";
    private final String VALUE_DELIMETER_IlMOITTAUTUMISTILA = "_ILMOITTAUTUMISTILA_";
    private final String VALUE_DELIMETER_JULKAISTAVISSA = "_JULKAISTAVISSA_";
    private final String VALUE_DELIMETER_VALINTATAPAJONOOID = "_VALINTATAPAJONOOID_";

    public void hash(Hasher hf) {
        valintatulos.stream().forEach(v -> {
            Supplier<Void> undefined = () -> {
                hf.putUnencodedChars(SijoitteluajoWrapper.VALUE_FOR_HASH_FUNCTION_WHEN_UNDEFINED);
                return null;
            };
            Function<String, Supplier<Void>> delimeter = dm -> {
                return () -> {
                    hf.putUnencodedChars(dm);
                    return null;
                };
            };
            delimeter.apply(VALUE_DELIMETER_VALINTATULOS).get();
            ifPresentOrIfNotPresent(v.getHakemusOid(), t -> hf.putUnencodedChars(t), undefined, delimeter.apply(VALUE_DELIMETER_HAKEMUSOID));
            ifPresentOrIfNotPresent(v.getHakukohdeOid(), t -> hf.putUnencodedChars(t), undefined, delimeter.apply(VALUE_DELIMETER_HAKUKOHDE));
            hf.putInt(v.getHakutoive());
            delimeter.apply(VALUE_DELIMETER_HAKUTOIVE).get();
            delimeter.apply(VALUE_DELIMETER_HYVAKSYTTY_VARASIJALTA).get();
            if (v.getIlmoittautumisTila() != null) {
                hf.putInt(v.getIlmoittautumisTila().ordinal());
            } else {
                // hf.putInt(-1); // jotain milla erotetaan null jostain arvosta
                undefined.get();
            }
            delimeter.apply(VALUE_DELIMETER_IlMOITTAUTUMISTILA).get();
            hf.putBoolean(v.getJulkaistavissa());
            delimeter.apply(VALUE_DELIMETER_JULKAISTAVISSA).get();
            if (v.getTila() != null) {
                hf.putInt(v.getTila().ordinal());
            } else {
                // hf.putInt(-1); // jotain milla erotetaan null jostain arvosta
                undefined.get();
            }
            ifPresentOrIfNotPresent(v.getValintatapajonoOid(), t -> hf.putUnencodedChars(t), undefined, delimeter.apply(VALUE_DELIMETER_VALINTATAPAJONOOID));
        });
    }
}
