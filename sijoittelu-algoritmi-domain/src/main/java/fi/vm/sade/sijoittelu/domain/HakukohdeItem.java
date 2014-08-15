package fi.vm.sade.sijoittelu.domain;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Embedded
public class HakukohdeItem implements Serializable {

    @SuppressWarnings("unused")
    @Id
    private ObjectId id;

    //hakukohdeoid
    private String oid;


   // @Reference(value = "Hakukohde", lazy = true)
   // private Hakukohde hakukohde;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

   // public Hakukohde getHakukohde() {
   //     return hakukohde;
   // }

   // public void setHakukohde(Hakukohde hakukohde) {
   //     this.hakukohde = hakukohde;
   // }

}
