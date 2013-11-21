package fi.vm.sade.sijoittelu.tulos.dto;

/**
 * User: wuoti
 * Date: 26.4.2013
 * Time: 16.00
 */
public class JsonViews {
    public static class All { }

    public static class Tila extends All{ }
    public static class MonenHakemuksenTila extends All { }

    public static class Hakukohde extends All{ }
    public static class Hakemus extends All{ }
    public static class Sijoitteluajo extends All{ }
    public static class Sijoittelu extends All{ }

    public static class Hakija extends All{ }
}
