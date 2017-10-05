package fi.vm.sade.sijoittelu.laskenta.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class SijoitteluBookkeeper {

    //Key: hakuOid, value: (sijoitteluId, kyseisen sijoittelun tila)
    private static volatile ConcurrentHashMap<String, SijoittelunIdJaTilaYhdiste> hakujenAjot = new ConcurrentHashMap<>();
    //Osin kaksinkertainen kirjanpito siksi, että voidaan helposti pollata käyttäen sijoitteluid:tä indeksinä.
    private static volatile ConcurrentHashMap<Long, String> kaikkiSijoitteluAjot = new ConcurrentHashMap<>();

    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluBookkeeper.class);

    private static SijoitteluBookkeeper instance = new SijoitteluBookkeeper();

    private SijoitteluBookkeeper() {}

    public static SijoitteluBookkeeper getInstance() {
        return instance;
    }

    public boolean luoUusiSijoitteluAjo(String hakuOid, Long sijoitteluId) {
        //Uuden sijoitteluajon voi luoda haulle, jos edellinen on päättynyt (VALMIS, VIRHE) tai sitä ei ole olemassa
        if(hakujenAjot.containsKey(hakuOid)) {
            if(!hakujenAjot.get(hakuOid).valmisTaiVirhe()) {
                LOGGER.warn("Yritettiin luoda haulle {} uusi sijoitteluajo id:llä {}, mutta edellinen ei ole vielä päättynyt (id {}, tila {}). Ei luotu uutta.", hakuOid, sijoitteluId, hakujenAjot.get(hakuOid).getSijoitteluId(), hakujenAjot.get(hakuOid).getTila() );
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
                LOGGER.error("Sijoitteluajon {} tilan {} asettaminen haulle {} ei onnistunut jostain syystä", sijoitteluId, tila, hakuOid);
            }
        } else {
            LOGGER.warn("Yritettiin muuttaa olemattoman sijoittelun tilaa haulle {}. Tämä saattaa indikoida ongelmaa. ", hakuOid);
        }
    }

    public SijoittelunIdJaTilaYhdiste getHaunSijoitteluAjo(String hakuOid) {
        if(hakujenAjot.containsKey(hakuOid)) {
            return hakujenAjot.get(hakuOid);
        } else {
            LOGGER.warn("Haulle {) ei löytynyt sijoitteluajoa", hakuOid);
            return null;
        }
    }

    public String getHaunSijoitteluajonTila(String hakuOid) {
        if(hakujenAjot.containsKey(hakuOid)) {
            return hakujenAjot.get(hakuOid).getTila();
        } else {
            LOGGER.warn("Haulle {) ei löytynyt sijoitteluajoa", hakuOid);
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
        private Long sijoitteluId;
        private String tila;
        private boolean valmisTaiVirhe;

        public SijoittelunIdJaTilaYhdiste(String hakuOid, Long sijoitteluId) {
            this.hakuOid = hakuOid;
            this.sijoitteluId = sijoitteluId;
            this.tila = HaunSijoittelunTila.KESKEN;
            valmisTaiVirhe = false;
        }
        //Jos tila on ennestään VALMIS tai VIRHE, sijoitteluajo on päättynyt eikä tilaa voida enää jälkeenpäin muuttaa.
        public boolean setTila(String tila) {
            if(this.valmisTaiVirhe) {
                LOGGER.warn("Haun {} sijoittelun {} tilaa ei voi enää muuttaa, koska se on tilassa {}", this.hakuOid, this.sijoitteluId, this.tila);
                return false;
            } else {
                LOGGER.info("Muutetaan haun {} sijoittelun {} tilaa. Vanha: {}, Uusi: {}", hakuOid, sijoitteluId, this.tila, tila);
                this.tila = tila;
                if(HaunSijoittelunTila.VALMIS.equals(tila) || HaunSijoittelunTila.VIRHE.equals(tila)) {
                    valmisTaiVirhe = true;
                }
                return true;
            }
        }
        public String getTila() {
            return tila;
        }
        public Long getSijoitteluId() {
            return sijoitteluId;
        }

        public boolean valmisTaiVirhe() {
            return this.valmisTaiVirhe;
        }
    }
}
