package fi.vm.sade.sijoittelu.domain;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 20.5.2013
 * Time: 13:32
 * To change this template use File | Settings | File Templates.
 */
public enum ValintatuloksenTila {

   // EI_HYVAKSYTTAVISSA,  // EI voi ottaa paikkaa vastaan, voi tulla hyvaksytyksi jossain vaiheessa jos sopiva tila muutos tulee
   // kaytannossa tila on silloin jos henkilo varalla tai valittu parempaan paikkaan
   // HYVAKSYTTAVISSA, //Hakija voi ottaa paikan vastaan

    OPPIJA_HYVAKSYNYT,   //Oppija hyvaksynyt paikan
    OPPIJA_PERUNUT,      //Oppija perunut paikan
    VIRKAILJA_HYVAKSYNYT,//Hakija hyvaksynyt paikan, virkailija merkinnyt hyvaksytyksi
    VIRKAILIJA_PERUNUT,  //Virkailija merkinnyt etta hakija ei halua paikkaa
    VIRKAILIJA_HYLANNYT; //Virkalija on merkinnyt hakemuksen jonossa hylatyksi
}
