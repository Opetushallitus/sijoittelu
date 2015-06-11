package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import java.util.ArrayList;
import java.util.List;

public class HakijaPaginationObject {
    private int totalCount = 0;
    private List<HakijaDTO> results = new ArrayList<HakijaDTO>(§);

    public Integer getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public List<HakijaDTO> getResults() {
        return results;
    }

    public void setResults(List<HakijaDTO> results) {
        this.results = results;
    }
}
