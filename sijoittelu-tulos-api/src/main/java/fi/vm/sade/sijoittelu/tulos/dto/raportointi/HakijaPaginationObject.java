package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 21.11.2013
 * Time: 15:28
 * To change this template use File | Settings | File Templates.
 */
public class HakijaPaginationObject {

    private Integer totalCount;

    private List<HakijaDTO> results;

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
