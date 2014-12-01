package fi.vm.sade.sijoittelu.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ValintatulosMailStatus {
    public Date previousCheck;
    public Date sent;
    public List<String> media = new ArrayList<String>();
}
