package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ValintatapajonoWrapper {

    private Valintatapajono valintatapajono;

    private HakukohdeWrapper hakukohdeWrapper;

    // Yhden sijoittelukierroksen aikainen lukko, jos alitäyttösääntö on lauennut hakijaryhmäkäsittelyssä
    private boolean alitayttoLukko = false;

    private List<HakemusWrapper> hakemukset = new ArrayList<>();

    public void setHakemukset(List<HakemusWrapper> hakemukset) {
        this.hakemukset = hakemukset;
    }

    public List<HakemusWrapper> getHakemukset() {
        return hakemukset;
    }

    public Stream<HakemusWrapper> ehdollisestiVastaanottaneetJonossa() {
        return hakemukset.stream().filter(h -> h.getHenkilo().getValintatulos().stream()
                .anyMatch(v -> v.getValintatapajonoOid().equals(valintatapajono.getOid())
                        && v.getTila() == ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT));
    }

    public Valintatapajono getValintatapajono() {
        return valintatapajono;
    }

    public void setValintatapajono(Valintatapajono valintatapajono) {
        this.valintatapajono = valintatapajono;
    }

    public HakukohdeWrapper getHakukohdeWrapper() {
        return hakukohdeWrapper;
    }

    public void setHakukohdeWrapper(HakukohdeWrapper hakukohdeWrapper) {
        this.hakukohdeWrapper = hakukohdeWrapper;
    }

    public boolean isAlitayttoLukko() {
        return alitayttoLukko;
    }

    public void setAlitayttoLukko(boolean alitayttoLukko) {
        this.alitayttoLukko = alitayttoLukko;
    }

    public Optional<HakemusWrapper> findHakemus(String hakemusOid) {
        return hakemukset.stream().filter(h -> hakemusOid.equals(h.getHakemus().getHakemusOid())).findFirst();
    }

    public SijoitteluConfiguration getSijoitteluConfiguration() {
        return getHakukohdeWrapper().getSijoitteluajoWrapper().sijoitteluConfiguration;
    }
}
