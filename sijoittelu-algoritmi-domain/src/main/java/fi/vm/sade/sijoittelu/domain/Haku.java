package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Embedded;
import org.codehaus.jackson.map.annotate.JsonView;

@Embedded
public class Haku {

    @JsonView(JsonViews.Basic.class)
    private String oid;
  
    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
