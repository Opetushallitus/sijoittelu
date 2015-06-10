package fi.vm.sade.sijoittelu.domain;

public enum HakemuksenTila {
    HYLATTY, // hakija ei voi koskaan tulla valituksi kohteeseen

    VARALLA, // Hakija voi tulla kohteeseen valituksi (jossain vaiheessa)

    PERUUNTUNUT, // Hakija on tullut valituksi parempaan paikkaan (korkeampi hakutoive)

    VARASIJALTA_HYVAKSYTTY, //Hakija voi ottaa paikan vastaan (alunperin varasijalla)

    HYVAKSYTTY, //Hakija voi ottaa paikan vastaan

    PERUNUT, //Hakija ei ole vastaanottanut paikkaa. Hakija ei voi tulla enää valituksi matalamman prioriteetin kohteissa

    PERUUTETTU; // Virkailija on perunut paikan. Sama toiminnallisuuks kuil HYLATTY
}
