package fi.vm.sade.sijoittelu.laskenta.mapping;

import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
/**
 * User: wuoti
 * Date: 9.12.2013
 * Time: 9.18
 */
public class SijoitteluModelMapper extends ModelMapper {

    public SijoitteluModelMapper() {
        super();

    }

    public <FROM, TO> List<TO> mapList(List<FROM> list, final Class<TO> to) {

        List<TO> toList = new ArrayList<TO>();

        for (FROM f : list) {
            toList.add(map(f, to));
        }

        return toList;
    }

}
