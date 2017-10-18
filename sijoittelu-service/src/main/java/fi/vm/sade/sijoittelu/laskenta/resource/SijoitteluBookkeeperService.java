package fi.vm.sade.sijoittelu.laskenta.resource;

import fi.vm.sade.sijoittelu.domain.HaunSijoittelunTila;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class SijoitteluBookkeeperService {

    //Key: hakuOid, value: (sijoitteluAjoId, kyseisen sijoittelun tila)
    private static volatile ConcurrentHashMap<String, SijoittelunIdJaTilaYhdiste> hakujenAjot = new ConcurrentHashMap<>();
    //Osin kaksinkertainen kirjanpito siksi, että voidaan helposti pollata käyttäen sijoitteluid:tä indeksinä.
    private static volatile ConcurrentHashMap<Long, String> kaikkiSijoitteluAjot = new ConcurrentHashMap<>();

    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluBookkeeperService.class);

    private static SijoitteluBookkeeperService instance = new SijoitteluBookkeeperService();

    private SijoitteluBookkeeperService() {}

    public static SijoitteluBookkeeperService getInstance() {
        return instance;
    }

    public boolean luoUusiSijoitteluAjo(String hakuOid, Long sijoitteluId) {
        //Uuden sijoitteluajon voi luoda haulle vain, jos edellinen on päättynyt (tilassa VALMIS tai VIRHE), tai sitä ei ole olemassa
        if(hakujenAjot.containsKey(hakuOid)) {
            if(!hakujenAjot.get(hakuOid).valmisTaiVirhe()) {
                LOGGER.warn("Yritettiin luoda haulle {} uusi sijoitteluajo id:llä {}, mutta edellinen ei ole vielä päättynyt (id {}, tila {}). Ei luotu uutta.", hakuOid, sijoitteluId, hakujenAjot.get(hakuOid).getSijoitteluAjoId(), hakujenAjot.get(hakuOid).getTila() );
                return false;
            }
        }
        LOGGER.info("Luodaan haulle {} uusi sijoitteluajo id:llä {}.", hakuOid, sijoitteluId);
        hakujenAjot.put(hakuOid, new SijoittelunIdJaTilaYhdiste(hakuOid, sijoitteluId));
        kaikkiSijoitteluAjot.put(sijoitteluId, HaunSijoittelunTila.KESKEN);
        return true;
    }

    public boolean haunSijoitteluValmisTaiVirhe(String hakuOid) {
        if(hakujenAjot.containsKey(hakuOid)) {
            return hakujenAjot.get(hakuOid).valmisTaiVirhe();
        } else {
            LOGGER.warn("Kysyttiin, onko olematon sijoittelu jo päättynyt. Tämä saattaa indikoida ongelmaa.");
            return false;
        }
    }

    public void merkitseSijoitteluAjonTila(String hakuOid, Long sijoitteluId, String tila) {
        if(hakujenAjot.containsKey(hakuOid)) {
            if(hakujenAjot.get(hakuOid).setTila(tila)) {
                kaikkiSijoitteluAjot.put(sijoitteluId, tila);
            } else {
                LOGGER.warn("Sijoitteluajon {} tilan {} asettaminen haulle {} ei onnistunut jostain syystä", sijoitteluId, tila, hakuOid);
            }
        } else {
            LOGGER.warn("Yritettiin muuttaa olemattoman sijoittelun tilaa haulle {}. Tämä saattaa indikoida ongelmaa. ", hakuOid);
        }
    }

    public SijoittelunIdJaTilaYhdiste getHaunSijoitteluAjo(String hakuOid) {
        if(hakujenAjot.containsKey(hakuOid)) {
            return hakujenAjot.get(hakuOid);
        } else {
            LOGGER.warn("Haulle {} ei löytynyt sijoitteluajoa", hakuOid);
            return null;
        }
    }

    public String getHaunSijoitteluajonTila(String hakuOid) {
        if(hakujenAjot.containsKey(hakuOid)) {
            return hakujenAjot.get(hakuOid).getTila();
        } else {
            LOGGER.warn("Haulle {} ei löytynyt sijoitteluajoa", hakuOid);
            return HaunSijoittelunTila.EI_LOYTYNYT;
        }
    }

    public String getSijoitteluAjonTila(Long sijoitteluId) {
        if(kaikkiSijoitteluAjot.containsKey(sijoitteluId)) {
            return kaikkiSijoitteluAjot.get(sijoitteluId);
        } else {
            return HaunSijoittelunTila.EI_LOYTYNYT;
        }
    }

    public class SijoittelunIdJaTilaYhdiste {

        private String hakuOid;
        private Long sijoitteluAjoId;
        private String tila;
        private boolean valmisTaiVirhe;

        public SijoittelunIdJaTilaYhdiste(String hakuOid, Long sijoitteluAjoId) {
            this.hakuOid = hakuOid;
            this.sijoitteluAjoId = sijoitteluAjoId;
            this.tila = HaunSijoittelunTila.KESKEN;
            valmisTaiVirhe = false;
        }
        //Jos tila on ennestään VALMIS tai VIRHE, sijoitteluajo on päättynyt eikä tilaa voida enää jälkeenpäin muuttaa.
        public boolean setTila(String tila) {
            if (this.valmisTaiVirhe) {
                LOGGER.warn("Haun {} sijoittelun {} tilaa ei voi enää muuttaa, koska se on tilassa {}", this.hakuOid, this.sijoitteluAjoId, this.tila);
                return false;
            } else {
                LOGGER.info("Muutetaan haun {} sijoittelun {} tilaa. Vanha: {}, Uusi: {}", hakuOid, sijoitteluAjoId, this.tila, tila);
                this.tila = tila;
                if (HaunSijoittelunTila.VALMIS.equals(tila) || HaunSijoittelunTila.VIRHE.equals(tila)) {
                    valmisTaiVirhe = true;
                }
                return true;
            }
        }
        public String getTila() {
            return tila;
        }
        public Long getSijoitteluAjoId() {
            return sijoitteluAjoId;
        }
        public boolean valmisTaiVirhe() {
            return this.valmisTaiVirhe;
        }
    }
}
