package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.sijoittelu.domain.SijoitteluajonTila;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class SijoitteluBookkeeperService {

    //Key: hakuOid, value: (sijoitteluAjoId, kyseisen sijoittelun tila)
    private static volatile ConcurrentHashMap<String, SijoitteluAjoIdJaTilaYhdiste> hakujenAjot = new ConcurrentHashMap<>();
    //Osin kaksinkertainen kirjanpito siksi, että voidaan helposti pollata käyttäen sijoitteluajoid:tä indeksinä.
    private static volatile ConcurrentHashMap<Long, SijoitteluajonTila> kaikkiSijoitteluAjot = new ConcurrentHashMap<>();

    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluBookkeeperService.class);

    public boolean luoUusiSijoitteluAjo(String hakuOid, Long sijoitteluajoId) {
        //Uuden sijoitteluajon voi luoda haulle vain, jos edellinen on päättynyt (tilassa VALMIS tai VIRHE), tai sitä ei ole olemassa
        if (hakujenAjot.containsKey(hakuOid)) {
            if (!hakujenAjot.get(hakuOid).tilaValmisTaiVirhe()) {
                LOGGER.error("Yritettiin luoda haulle {} uusi sijoitteluajo id:llä {}, mutta edellinen ei ole vielä päättynyt (id {}, tila {}). Ei luotu uutta.", hakuOid, sijoitteluajoId, hakujenAjot.get(hakuOid).getSijoitteluAjoId(), hakujenAjot.get(hakuOid).getTila() );
                return false;
            }
        }
        LOGGER.info("Luodaan haulle {} uusi sijoitteluajo id:llä {}.", hakuOid, sijoitteluajoId);
        hakujenAjot.put(hakuOid, new SijoitteluAjoIdJaTilaYhdiste(hakuOid, sijoitteluajoId));
        kaikkiSijoitteluAjot.put(sijoitteluajoId, SijoitteluajonTila.KESKEN);
        return true;
    }

    public void merkitseSijoitteluAjonTila(String hakuOid, Long sijoitteluajoId, SijoitteluajonTila tila) {
        if (hakujenAjot.containsKey(hakuOid)) {
            if (hakujenAjot.get(hakuOid).setTila(tila)) {
                kaikkiSijoitteluAjot.put(sijoitteluajoId, tila);
            } else {
                LOGGER.warn("Sijoitteluajon {} tilan {} asettaminen haulle {} ei onnistunut jostain syystä", sijoitteluajoId, tila, hakuOid);
            }
        } else {
            LOGGER.warn("Yritettiin muuttaa olemattoman sijoittelun tilaa haulle {}. Tämä saattaa indikoida ongelmaa. ", hakuOid);
        }
    }

    public SijoitteluAjoIdJaTilaYhdiste getHaunSijoitteluAjo(String hakuOid) {
        if (hakujenAjot.containsKey(hakuOid)) {
            return hakujenAjot.get(hakuOid);
        } else {
            LOGGER.warn("Haulle {} ei löytynyt sijoitteluajoa", hakuOid);
            return null;
        }
    }

    public String getHaunSijoitteluajonTila(String hakuOid) {
        if (hakujenAjot.containsKey(hakuOid)) {
            return hakujenAjot.get(hakuOid).getTila().toString();
        } else {
            LOGGER.warn("Haulle {} ei löytynyt sijoitteluajoa", hakuOid);
            return SijoitteluajonTila.EI_LOYTYNYT.toString();
        }
    }

    public String getSijoitteluAjonTila(Long sijoitteluajoId) {
        if (kaikkiSijoitteluAjot.containsKey(sijoitteluajoId)) {
            return kaikkiSijoitteluAjot.get(sijoitteluajoId).toString();
        } else {
            return SijoitteluajonTila.EI_LOYTYNYT.toString();
        }
    }

    public class SijoitteluAjoIdJaTilaYhdiste {
        private String hakuOid;
        private Long sijoitteluAjoId;
        private SijoitteluajonTila tila;

        public SijoitteluAjoIdJaTilaYhdiste(String hakuOid, Long sijoitteluAjoId) {
            this.hakuOid = hakuOid;
            this.sijoitteluAjoId = sijoitteluAjoId;
            this.tila = SijoitteluajonTila.KESKEN;
        }
        //Jos tila on ennestään VALMIS tai VIRHE, sijoitteluajo on päättynyt eikä tilaa voida enää jälkeenpäin muuttaa.
        public boolean setTila(SijoitteluajonTila tila) {
            if (tilaValmisTaiVirhe()) {
                LOGGER.warn("Haun {} sijoittelun {} tilaa ei voi enää muuttaa, koska se on tilassa {}", this.hakuOid, this.sijoitteluAjoId, this.tila);
                return false;
            } else {
                LOGGER.info("Muutetaan haun {} sijoittelun {} tilaa. Vanha: {}, Uusi: {}", hakuOid, sijoitteluAjoId, this.tila, tila);
                this.tila = tila;
                return true;
            }
        }

        public SijoitteluajonTila getTila() {
            return this.tila;
        }

        public Long getSijoitteluAjoId() {
            return sijoitteluAjoId;
        }

        public boolean tilaValmisTaiVirhe() {
            if (this.tila == SijoitteluajonTila.VALMIS || this.tila == SijoitteluajonTila.VIRHE) {
                return true;
            }
            return false;
        }
    }
}
