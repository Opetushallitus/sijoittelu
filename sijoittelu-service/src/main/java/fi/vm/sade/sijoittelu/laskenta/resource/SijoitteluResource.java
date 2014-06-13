package fi.vm.sade.sijoittelu.laskenta.resource;

import com.google.common.base.Predicates;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import fi.vm.sade.service.valintaperusteet.schema.TasasijasaantoTyyppi;
import fi.vm.sade.service.valintatiedot.schema.*;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.valintalaskenta.domain.dto.*;
import fi.vm.sade.valintalaskenta.tulos.service.ValintalaskentaTulosService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import java.math.BigDecimal;
import java.util.*;

import static fi.vm.sade.sijoittelu.laskenta.roles.SijoitteluRole.CRUD;

@Path("sijoittele")
@Component
@PreAuthorize("isAuthenticated()")
@Api(value = "/tila", description = "Resurssi sijoitteluun")
public class SijoitteluResource {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(SijoitteluResource.class);

	@Autowired
	private SijoitteluBusinessService sijoitteluBusinessService;

    @Autowired
    private ValintalaskentaTulosService tulosService;

	@GET
	@Path("{hakuOid}")
	@PreAuthorize(CRUD)
	@ApiOperation(value = "Hakemuksen valintatulosten haku")
	public String sijoittele(@PathParam("hakuOid") String hakuOid) {

        LOGGER.error("Valintatietoja valmistetaan haulle {}!", hakuOid);
        List<HakukohdeDTO> a = tulosService
                .haeLasketutValinnanvaiheetHaulle(hakuOid);
        LOGGER.error("Valintatiedot haettu serviceltä {}!", hakuOid);

        HakuTyyppi haku = new HakuTyyppi();
        haku.setHakuOid(hakuOid);

        LOGGER.error("Konvertoidaan hakutyypeiksi {}!", hakuOid);
        for (HakukohdeDTO v : a) {
            HakukohdeTyyppi ht = new HakukohdeTyyppi();
            ht.setOid(v.getOid());
            ht.setTarjoajaOid(v.getTarjoajaoid());
            haku.getHakukohteet().add(ht);

            LOGGER.error("Konvertoidaan Valinnanvaiheet {}!", hakuOid);
            for (ValinnanvaiheDTO valinnanvaiheDTO : v.getValinnanvaihe()) {
                ht.getValinnanvaihe().add(
                        createValinnanvaiheTyyppi(valinnanvaiheDTO));

            }
        }
        LOGGER.error("Palautetaan valintatiedot {} hakukohteella!", haku
                .getHakukohteet().size());
        try {
            sijoitteluBusinessService.sijoittele(haku);
            LOGGER.error("Sijoittelu suoritettu onnistuneesti!");
        } catch (Exception e) {
            LOGGER.error("Sijoittelu epäonnistui syystä {}!\r\n{}",
                    e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
        return "true";
	}


    // Täysin turhaa roskaa, korjataan kun saadaan sijoteltua
    private ValinnanvaiheTyyppi createValinnanvaiheTyyppi(
            ValinnanvaiheDTO valinnanvaihe) {
        ValinnanvaiheTyyppi v = new ValinnanvaiheTyyppi();
        v.setValinnanvaihe(valinnanvaihe.getJarjestysnumero());
        v.setValinnanvaiheOid(valinnanvaihe.getValinnanvaiheoid());
        for (ValintatapajonoDTO vt : valinnanvaihe.getValintatapajonot()) {
            v.getValintatapajono().add(createValintatapajonoTyyppi(vt));
        }
        return v;
    }

    private ValintatapajonoTyyppi createValintatapajonoTyyppi(
            ValintatapajonoDTO vt) {
        ValintatapajonoTyyppi valintatapajonoTyyppi = new ValintatapajonoTyyppi();
        valintatapajonoTyyppi.setOid(vt.getOid());
        valintatapajonoTyyppi.setAloituspaikat(vt.getAloituspaikat());
        valintatapajonoTyyppi.setNimi(vt.getNimi());
        valintatapajonoTyyppi.setPrioriteetti(vt.getPrioriteetti());
        valintatapajonoTyyppi.setSiirretaanSijoitteluun(vt
                .isSiirretaanSijoitteluun());
        valintatapajonoTyyppi.setEiVarasijatayttoa(vt.getEiVarasijatayttoa());
        valintatapajonoTyyppi.setKaikkiEhdonTayttavatHyvaksytaan(vt
                .getKaikkiEhdonTayttavatHyvaksytaan());
        valintatapajonoTyyppi.setPoissaOlevaTaytto(vt.getPoissaOlevaTaytto());
        if (vt.getTasasijasaanto() != null) {
            valintatapajonoTyyppi.setTasasijasaanto(TasasijasaantoTyyppi
                    .valueOf(vt.getTasasijasaanto().name()));
        }

        for (JonosijaDTO jonosija : vt.getJonosijat()) {
            HakijaTyyppi ht = new HakijaTyyppi();
            ht.setPrioriteetti(jonosija.getPrioriteetti());

            if (jonosija.getTuloksenTila() == null) {
                ht.setTila(HakemusTilaTyyppi.MAARITTELEMATON);
            } else {
                ht.setTila(HakemusTilaTyyppi.valueOf(jonosija.getTuloksenTila()
                        .name()));
            }

            ht.setHakemusOid(jonosija.getHakemusOid());
            ht.setEtunimi(jonosija.getEtunimi());
            ht.setSukunimi(jonosija.getSukunimi());
            ht.setOid(jonosija.getHakijaOid());
            ht.setJonosija(jonosija.getJonosija());
            for (SyotettyArvoDTO sa : jonosija.getSyotetytArvot()) {
                ht.getSyotettyArvo().add(createSyotettyArvoTyyppi(sa));

            }

            if (jonosija.isHarkinnanvarainen()) {
                ht.setHarkinnanvarainen(Boolean.TRUE);
            }

            List<JarjestyskriteeritulosDTO> kriteerit = FluentIterable
                    .from(jonosija.getJarjestyskriteerit())
                    .filter(Predicates.notNull()).toList();

            if (!kriteerit.isEmpty()) {
                JarjestyskriteeritulosDTO merkityksellisinKriteeri = kriteerit
                        .get(0);
                try {
                    if (merkityksellisinKriteeri.getKuvaus() != null
                            || !merkityksellisinKriteeri.getKuvaus().isEmpty()) {
                        ht.getTilanKuvaus().addAll(
                                convertKuvaus(merkityksellisinKriteeri
                                        .getKuvaus()));
                    }
                } catch (Exception e) {
                    LOGGER.error(
                            "ValintatapajonoOid({}) Järjestyskriteerille ei voitu luoda kuvausta: {}",
                            vt.getOid(), e.getMessage());

                }
                try {
                    BigDecimal arvo = merkityksellisinKriteeri.getArvo();
                    if (arvo == null) {
                        ht.setPisteet(StringUtils.EMPTY);
                    } else {
                        ht.setPisteet(arvo.toString());
                    }
                } catch (Exception e) {
                    LOGGER.error(
                            "ValintatapajonoOid({}) Järjestyskriteerille ei voitu luoda pisteitä: {}",
                            vt.getOid(), e.getMessage());
                    ht.setPisteet(StringUtils.EMPTY);
                }

            }

            valintatapajonoTyyppi.getHakija().add(ht);
        }
        return valintatapajonoTyyppi;
    }

    private Collection<AvainArvoTyyppi> convertKuvaus(Map<String, String> kuvaus) {
        Collection<AvainArvoTyyppi> a = Lists.newArrayList();

        for (Map.Entry<String, String> keyValuePair : kuvaus.entrySet()) {
            AvainArvoTyyppi a0 = new AvainArvoTyyppi();
            a0.setAvain(keyValuePair.getKey());
            a0.setArvo(keyValuePair.getValue());
            a.add(a0);
        }

        return a;
    }

    private SyotettyArvoTyyppi createSyotettyArvoTyyppi(SyotettyArvoDTO sa) {
        SyotettyArvoTyyppi tyyppi = new SyotettyArvoTyyppi();
        tyyppi.setArvo(sa.getArvo());
        tyyppi.setLaskennallinenArvo(sa.getLaskennallinenArvo());
        tyyppi.setOsallistuminen(sa.getOsallistuminen());
        tyyppi.setTunniste(sa.getTunniste());
        return tyyppi;
    }


}
