package fi.vm.sade.sijoittelu.domain;

/**
 * 
 * @author Kari Kammonen
 *
 */
public enum HakemuksenTila {

    HYLATTY, // hakija ei voi koskaan tulla valituksi kohteeseen
    
    VARALLA, // Hakija voi tulla kohteeseen valituksi (jossain vaiheessa)
    
    PERUUNTUNUT, // Hakija on tullut valituksi parempaan paikkaan
        
    HYVAKSYTTY; //HAkija voi ottaa paikan vastaan


}
