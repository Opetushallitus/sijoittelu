package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

public class HakuDTO {
    private String kohdejoukkoUri;
    private String kohdejoukonTarkenne;

    public HakuDTO(){}

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
}
