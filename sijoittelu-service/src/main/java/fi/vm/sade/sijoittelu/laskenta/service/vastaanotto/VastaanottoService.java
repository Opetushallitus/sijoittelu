package fi.vm.sade.sijoittelu.laskenta.service.vastaanotto;

import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.ILMOITETTU;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.PERUNUT;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.VASTAANOTTANUT;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.domain.ValintatulosPerustiedot;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;

@Service
public class VastaanottoService {
    private ValintatulosDao dao;

    @Autowired
    public VastaanottoService(final ValintatulosDao dao) {
        this.dao = dao;
    }

    public void vastaanota(ValintatulosPerustiedot perustiedot, ValintatuloksenTila tila) {
        if (!Arrays.asList(VASTAANOTTANUT, EHDOLLISESTI_VASTAANOTTANUT, PERUNUT).contains(tila)) {
            throw new IllegalArgumentException("Ei-hyväksytty vastaantottotila: " + tila);
        }
        Valintatulos valintatulos = dao.loadValintatulos(perustiedot.hakukohdeOid, perustiedot.valintatapajonoOid, perustiedot.hakemusOid);
        if (valintatulos == null) {
            valintatulos = perustiedot.createValintatulos(tila);
        } else if (valintatulos.getTila() != ILMOITETTU) {
            if (valintatulos.getTila() != EHDOLLISESTI_VASTAANOTTANUT) {
                throw new IllegalArgumentException("Vastaanotto ei mahdollista tilassa " + valintatulos.getTila());
            } else if (!Arrays.asList(VASTAANOTTANUT, PERUNUT).contains(tila)) {
                throw new IllegalArgumentException("Tilasta " + valintatulos.getTila() + " ei mahdollista siirtyä tilaan " + tila);
            }
        }
        dao.createOrUpdateValintatulos(valintatulos);
    }
}
