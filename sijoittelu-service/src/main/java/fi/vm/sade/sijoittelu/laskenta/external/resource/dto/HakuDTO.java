package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

/**
 * Created by kjsaila on 06/02/15.
 */
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
