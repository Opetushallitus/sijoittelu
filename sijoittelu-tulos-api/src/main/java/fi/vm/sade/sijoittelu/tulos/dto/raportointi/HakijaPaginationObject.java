package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.JsonViews;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 21.11.2013
 * Time: 15:28
 * To change this template use File | Settings | File Templates.
 */
public class HakijaPaginationObject {

    @JsonView({ JsonViews.All.class})
    private Integer totalCount;

    @JsonView({ JsonViews.All.class})
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
