package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

public class HakuDTO {
    private String kohdejoukkoUri;

    public HakuDTO(){}

    public String getKohdejoukkoUri() {
        return kohdejoukkoUri;
    }

    public void setKohdejoukkoUri(String kohdejoukkoUri) {
        this.kohdejoukkoUri = kohdejoukkoUri;
    }
}
