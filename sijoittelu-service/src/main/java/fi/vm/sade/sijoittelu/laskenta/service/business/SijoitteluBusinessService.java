package fi.vm.sade.sijoittelu.laskenta.service.business;

import java.util.List;
import java.util.Set;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;

public interface SijoitteluBusinessService {
    void sijoittele(HakuDTO sijoitteluTyyppi, Set<String> valintaperusteidenJonot);

    List<HakukohdeDTO> valisijoittele(HakuDTO sijoitteluTyyppi);

    long erillissijoittele(HakuDTO sijoitteluTyyppi);

    Valintatulos haeHakemuksenTila(String hakuoid, String hakukohdeOid, String valintatapajonoOid, String hakemusOid);

    List<Valintatulos> haeHakemuksenTila(String hakemusOid);

    Hakukohde getHakukohde(String hakuOid, String hakukohdeOid);

    void vaihdaHakemuksenTila(String hakuoid, Hakukohde hakukohde, String valintatapajonoOid, String hakemusOid,
                              ValintatuloksenTila tila, String selite, IlmoittautumisTila ilmoittautumisTila,
                              boolean julkaistavissa, boolean hyvaksyttyVarasijalta);

    List<Valintatulos> haeHakemustenTilat(String hakukohdeOid, String valintatapajonoOid);

    List<Valintatulos> haeHakukohteenTilat(String hakukohdeOid);

    boolean muutoksetOvatAjantasaisia(List<Valintatulos> valintatulokset);
}
