package fi.vm.sade.sijoittelu.domain;

/**
 * 
 * @author Kari Kammonen
 *
 */
public enum HakemuksenTila {
    
    HYLATTY, // kohde ei ole valittavissa
    
    VARALLA, // VARALLA on alkutila kaikille sijoiteltaville
    
    PERUUNTUNUT, //PERUUNTUNUT, käyttäjä peruuttanyt tai tullut valittua parempaan kohteeseen
        
    HYVAKSYTTY, // HYVÄKSYTTY, hakemus on hyväksytty. Tämä voi korvaantua PERUUNTUNYT, 
                //jos myohemmin sijoittelussa henkilo tulee valittua korkeamman prioriteetin paikkaan 
    
    ILMOITETTU, //ILMOITETTU, hakijalle on ilmoitettu. Tata tilaa ei enaa voi muuttaa 
}
