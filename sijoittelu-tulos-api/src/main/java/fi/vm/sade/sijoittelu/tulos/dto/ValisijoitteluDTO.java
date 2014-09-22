package fi.vm.sade.sijoittelu.tulos.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kjsaila on 21/09/14.
 */
public class ValisijoitteluDTO {
    private Map<String, List<String>> hakukohteet = new HashMap<String, List<String>>();

    public Map<String, List<String>> getHakukohteet() {
        return hakukohteet;
    }

    public void setHakukohteet(Map<String, List<String>> hakukohteet) {
        this.hakukohteet = hakukohteet;
    }
}
