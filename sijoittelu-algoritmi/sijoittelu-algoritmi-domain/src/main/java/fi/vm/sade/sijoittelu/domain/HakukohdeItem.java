package fi.vm.sade.sijoittelu.domain;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;

@Embedded
public class HakukohdeItem {

    @SuppressWarnings("unused")
    @Id
    private ObjectId id;

    private String oid;

    @Reference(value = "Hakukohde", lazy = true)
    private Hakukohde hakukohde;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Hakukohde getHakukohde() {
        return hakukohde;
    }

    public void setHakukohde(Hakukohde hakukohde) {
        this.hakukohde = hakukohde;
    }

}
