package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

public class HakuDTO {
    private String oid;
    private String kohdejoukkoUri;
    private String kohdejoukonTarkenne;
    private boolean usePriority = true;

    public HakuDTO(){}

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getOid() {
        return oid;
    }

    public String getKohdejoukkoUri() {
        return kohdejoukkoUri;
    }

    public void setKohdejoukkoUri(String kohdejoukkoUri) {
        this.kohdejoukkoUri = kohdejoukkoUri;
    }

    public String getKohdejoukonTarkenne() {
        return kohdejoukonTarkenne;
    }

    public void setKohdejoukonTarkenne(String kohdejoukonTarkenne) {
        this.kohdejoukonTarkenne = kohdejoukonTarkenne;
    }

    public boolean isUsePriority() {
        return usePriority;
    }

    public void setUsePriority(boolean usePriority) {
        this.usePriority = usePriority;
    }
}
