package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import com.google.common.collect.Sets;
import com.google.common.hash.Funnels;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;
import static java.util.stream.Stream.concat;
/**
 * 
 * @author Kari Kammonen
 * 
 */
public class HakukohdeWrapper implements Comparable<HakukohdeWrapper> {
    private static final Logger LOG = LoggerFactory.getLogger(HakukohdeWrapper.class);
    private Hakukohde hakukohde;

    private SijoitteluajoWrapper sijoitteluajoWrapper;

    private List<HakijaryhmaWrapper> hakijaryhmaWrappers = new ArrayList<HakijaryhmaWrapper>();

    private List<ValintatapajonoWrapper> valintatapajonot = new ArrayList<ValintatapajonoWrapper>();

    public List<ValintatapajonoWrapper> getValintatapajonot() {
        return valintatapajonot;
    }

    public void setValintatapajonot(List<ValintatapajonoWrapper> valintatapajonot) {
        this.valintatapajonot = valintatapajonot;
    }

    public Hakukohde getHakukohde() {
        return hakukohde;
    }

    public void setHakukohde(Hakukohde hakukohde) {
        this.hakukohde = hakukohde;
    }

    public SijoitteluajoWrapper getSijoitteluajoWrapper() {
        return sijoitteluajoWrapper;
    }

    public void setSijoitteluajoWrapper(SijoitteluajoWrapper sijoitteluajoWrapper) {
        this.sijoitteluajoWrapper = sijoitteluajoWrapper;
    }

    @Override
    public int compareTo(HakukohdeWrapper o) {
        if(hakukohde == null) {
            return -1; // null:t alkuun
        } else if(o == null || o.getHakukohde()==null) {
            return 1;
        }
        return hakukohde.getOid().compareTo(o.getHakukohde().getOid());
    }

    @Override
    public int hashCode() {
        if(hakukohde==null) {
            return 0;
        }
        return hakukohde.getOid().hashCode();
    }

    public List<HakijaryhmaWrapper> getHakijaryhmaWrappers() {
        return hakijaryhmaWrappers;
    }

    public Stream<HakemusWrapper> hakukohteenHakemukset() {
        return valintatapajonot.stream().flatMap(v -> v.getHakemukset().stream())
                .filter(Objects::nonNull).distinct();
    }
    public Stream<HenkiloWrapper> hakukohteenHakijat() {
        return concat(
                //henkilotJonoista
                valintatapajonot.stream().flatMap(v -> v.getHakemukset().stream()).map(h -> h.getHenkilo())
                ,
                //henkilotHakijaryhmista
                hakijaryhmaWrappers.stream().flatMap(h -> h.getHenkiloWrappers().stream())
        )
                .filter(Objects::nonNull)
                .distinct();
    }
}
