package fi.vm.sade.sijoittelu.jatkuva.dao.dto;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class SijoitteluDto {
    private static final DateTimeFormatter PVMFORMATTER = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");

    private final String hakuOid;
    private final boolean ajossa;
    private final Date viimeksiAjettu;
    private final String virhe;
    private Date aloitusajankohta;
    private Integer ajotiheys;

    public SijoitteluDto() {
        this.hakuOid = null;
        this.ajossa = false;
        this.viimeksiAjettu = null;
        this.virhe = null;
        this.aloitusajankohta = null;
        this.ajotiheys = null;
    }

    public SijoitteluDto(String hakuOid, boolean ajossa, Date viimeksiAjettu, String virhe, Date aloitusajankohta, Integer ajotiheys) {
        this.hakuOid = hakuOid;
        this.ajossa = ajossa;
        this.viimeksiAjettu = viimeksiAjettu;
        this.virhe = virhe;
        this.aloitusajankohta = aloitusajankohta;
        this.ajotiheys = ajotiheys;
    }

    public String getViimeksiAjettuFormatoituna() {
        if (viimeksiAjettu == null) {
            return null;
        }
        return PVMFORMATTER.print(viimeksiAjettu.getTime());
    }

    public String getAloitusajankohtaFormatoituna() {
        if (aloitusajankohta == null) {
            return null;
        }
        return PVMFORMATTER.print(aloitusajankohta.getTime());
    }

    public String getVirhe() {
        return virhe;
    }

    public String getHakuOid() {
        return hakuOid;
    }

    public Date getViimeksiAjettu() {
        return viimeksiAjettu;
    }

    public boolean isAjossa() {
        return ajossa;
    }

    public Date getAloitusajankohta() {
        return aloitusajankohta;
    }

    public void setAloitusajankohta(Date aloitusajankohta) {
        this.aloitusajankohta = aloitusajankohta;
    }

    public Integer getAjotiheys() {
        return ajotiheys;
    }

    public void setAjotiheys(Integer ajotiheys) {
        this.ajotiheys = ajotiheys;
    }
}
