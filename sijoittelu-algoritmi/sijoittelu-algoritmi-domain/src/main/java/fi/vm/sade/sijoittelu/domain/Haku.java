package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Embedded;

@Embedded
public class Haku {

    private String oid;
  
    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
