package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import static java.util.stream.Stream.concat;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

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
        if (hakukohde == null) {
            return -1; // null:t alkuun
        } else if (o == null || o.getHakukohde() == null) {
            return 1;
        }
        return hakukohde.getOid().compareTo(o.getHakukohde().getOid());
    }

    @Override
    public int hashCode() {
        if (hakukohde == null) {
            return 0;
        }
        return hakukohde.getOid().hashCode();
    }

    public List<HakijaryhmaWrapper> getHakijaryhmaWrappers() {
        return hakijaryhmaWrappers;
    }

    public void setHakijaryhmaWrappers(List<HakijaryhmaWrapper> hakijaryhmaWrappers) {
        this.hakijaryhmaWrappers = hakijaryhmaWrappers;
    }

    public Stream<HakemusWrapper> hakukohteenHakemukset() {
        return valintatapajonot.stream().flatMap(v -> v.getHakemukset().stream())
                .filter(Objects::nonNull).distinct();
    }

    public Stream<HenkiloWrapper> hakukohteenHakijat() {
        return concat(
                valintatapajonot.stream().flatMap(v -> v.getHakemukset().stream()).map(h -> h.getHenkilo()),
                hakijaryhmaWrappers.stream().flatMap(h -> h.getHenkiloWrappers().stream())
        ).filter(Objects::nonNull).distinct();
    }

    public Optional<ValintatapajonoWrapper> findValintatapajonoWrapper(String jonoOid) {
        return valintatapajonot.stream().filter(j -> jonoOid.equals(j.getValintatapajono().getOid())).findFirst();
    }
}
