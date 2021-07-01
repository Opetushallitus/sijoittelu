package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

public class KoutaHaku {
    public final String oid;
    public final String kohdejoukkoKoodiUri;
    public final String kohdejoukonTarkenneKoodiUri;

    private KoutaHaku() {
        this.oid = null;
        this.kohdejoukkoKoodiUri = null;
        this.kohdejoukonTarkenneKoodiUri = null;
    }
}
