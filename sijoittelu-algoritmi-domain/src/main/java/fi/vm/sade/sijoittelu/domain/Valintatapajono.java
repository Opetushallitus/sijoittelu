package fi.vm.sade.sijoittelu.domain;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYVAKSYTTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARASIJALTA_HYVAKSYTTY;
import static java.lang.Boolean.TRUE;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Valintatapajono implements Serializable {

    private Tasasijasaanto tasasijasaanto;

    private ValintatapajonoTila tila;

    private String oid;

    private String nimi;

    private Integer prioriteetti;

    private Integer aloituspaikat;

    private Integer alkuperaisetAloituspaikat;

    private Boolean eiVarasijatayttoa;

    private Boolean kaikkiEhdonTayttavatHyvaksytaan;

    private Boolean poissaOlevaTaytto;

    private Integer varasijat = 0;

    private Integer varasijaTayttoPaivat = 0;

    private Date varasijojaKaytetaanAlkaen;

    private Date varasijojaTaytetaanAsti;

    private String tayttojono;

    private Integer hyvaksytty;

    private Integer varalla;

    private BigDecimal alinHyvaksyttyPistemaara;

    private Boolean valintaesitysHyvaksytty;

    private Integer hakemustenMaara;

    private boolean sijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa;

    private Optional<JonosijaTieto> sivssnovSijoittelunVarasijataytonRajoitus = Optional.empty();

    private List<Hakemus> hakemukset = new ArrayList<Hakemus>();

    public Integer getHakemustenMaara() {
        return hakemustenMaara;
    }

    public void setHakemustenMaara(Integer hakemustenMaara) {
        this.hakemustenMaara = hakemustenMaara;
    }

    public ValintatapajonoTila getTila() {
        return tila;
    }

    public void setTila(ValintatapajonoTila tila) {
        this.tila = tila;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public Integer getPrioriteetti() {
        return prioriteetti;
    }

    public void setPrioriteetti(int prioriteetti) {
        this.prioriteetti = prioriteetti;
    }

    public Integer getAloituspaikat() {
        return aloituspaikat;
    }

    public void setAloituspaikat(int aloituspaikat) {
        this.aloituspaikat = aloituspaikat;
    }

    public List<Hakemus> getHakemukset() {
        return hakemukset;
    }

    public void setHakemukset(List<Hakemus> hakemukset) {
        this.hakemukset = hakemukset;
    }

    public Tasasijasaanto getTasasijasaanto() {
        return tasasijasaanto;
    }

    public void setTasasijasaanto(Tasasijasaanto tasasijasaanto) {
        this.tasasijasaanto = tasasijasaanto;
    }

    @Override
    public String toString() {
        return "Valintatapajono{" +
                "tasasijasaanto=" + tasasijasaanto +
                ", tila=" + tila +
                ", nimi=" + nimi +
                ", oid='" + oid + '\'' +
                ", prioriteetti=" + prioriteetti +
                ", aloituspaikat=" + aloituspaikat +
                ", hakemukset=" + hakemukset +
                '}';
    }

    public Boolean getEiVarasijatayttoa() {
        return eiVarasijatayttoa;
    }

    public void setEiVarasijatayttoa(Boolean eiVarasijatayttoa) {
        this.eiVarasijatayttoa = eiVarasijatayttoa;
    }

    public Boolean getKaikkiEhdonTayttavatHyvaksytaan() {
        return kaikkiEhdonTayttavatHyvaksytaan;
    }

    public void setKaikkiEhdonTayttavatHyvaksytaan(Boolean kaikkiEhdonTayttavatHyvaksytaan) {
        this.kaikkiEhdonTayttavatHyvaksytaan = kaikkiEhdonTayttavatHyvaksytaan;
    }

    public Boolean getPoissaOlevaTaytto() {
        return poissaOlevaTaytto;
    }

    public void setPoissaOlevaTaytto(Boolean poissaOlevaTaytto) {
        this.poissaOlevaTaytto = poissaOlevaTaytto;
    }

    public Integer getVarasijat() {
        return varasijat;
    }

    public void setVarasijat(Integer varasijat) {
        this.varasijat = varasijat;
    }

    public boolean rajoitettuVarasijaTaytto() {
        return varasijat != null && varasijat > 0;
    }

    public Integer getVarasijaTayttoPaivat() {
        return varasijaTayttoPaivat;
    }

    public void setVarasijaTayttoPaivat(Integer varasijaTayttoPaivat) {
        this.varasijaTayttoPaivat = varasijaTayttoPaivat;
    }

    public Date getVarasijojaKaytetaanAlkaen() {
        return varasijojaKaytetaanAlkaen;
    }

    public void setVarasijojaKaytetaanAlkaen(Date varasijojaKaytetaanAlkaen) {
        this.varasijojaKaytetaanAlkaen = varasijojaKaytetaanAlkaen;
    }

    public Date getVarasijojaTaytetaanAsti() {
        return varasijojaTaytetaanAsti;
    }

    public String getTayttojono() {
        return tayttojono;
    }

    public void setTayttojono(String tayttojono) {
        this.tayttojono = tayttojono;
    }

    public void setVarasijojaTaytetaanAsti(Date varasijojaTaytetaanAsti) {
        this.varasijojaTaytetaanAsti = varasijojaTaytetaanAsti;
    }

    public Integer getHyvaksytty() {
        return hyvaksytty;
    }

    public void setHyvaksytty(Integer hyvaksytty) {
        this.hyvaksytty = hyvaksytty;
    }

    public Integer getVaralla() {
        return varalla;
    }

    public void setVaralla(Integer varalla) {
        this.varalla = varalla;
    }

    public BigDecimal getAlinHyvaksyttyPistemaara() {
        return alinHyvaksyttyPistemaara;
    }

    public void setAlinHyvaksyttyPistemaara(BigDecimal alinHyvaksyttyPistemaara) {
        this.alinHyvaksyttyPistemaara = alinHyvaksyttyPistemaara;
    }

    public Integer getAlkuperaisetAloituspaikat() {
        return alkuperaisetAloituspaikat;
    }

    public void setAlkuperaisetAloituspaikat(Integer alkuperaisetAloituspaikat) {
        this.alkuperaisetAloituspaikat = alkuperaisetAloituspaikat;
    }

    public Boolean getValintaesitysHyvaksytty() {
        return valintaesitysHyvaksytty;
    }

    public void setValintaesitysHyvaksytty(Boolean valintaesitysHyvaksytty) {
        this.valintaesitysHyvaksytty = valintaesitysHyvaksytty;
    }

    public boolean getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa() {
        return sijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa;
    }

    public void setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(boolean sijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa) {
        this.sijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa = sijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa;
    }

    public Optional<JonosijaTieto> getSivssnovSijoittelunVarasijataytonRajoitus() {
        return sivssnovSijoittelunVarasijataytonRajoitus;
    }

    public void setSivssnovSijoittelunVarasijataytonRajoitus(Optional<JonosijaTieto> sivssnovSijoittelunVarasijataytonRajoitus) {
        sivssnovSijoittelunVarasijataytonRajoitus.ifPresent(jonosijaTieto -> {
            if (vapaaVarasijataytto()) {
                throw new IllegalArgumentException(String.format("Jonolla %s on vapaa varasijatäyttö, mutta sille oltiin " +
                    "asettamassa sivssnovSijoittelunVarasijataytonRajoitus == %s. Vaikuttaa bugilta.", oid, jonosijaTieto));
            }
            if (varasijat != null && varasijat > 0 && !VARALLA.equals(jonosijaTieto.tila)) {
                throw new IllegalArgumentException(String.format("Jonolla %s on rajattu varasijojen määrä (%d), mutta sille oltiin " +
                    "asettamassa sivssnovSijoittelunVarasijataytonRajoitus == %s, vaikka rajalla olevan hakemuksen pitäisi " +
                    "olla tilassa %s. Vaikuttaa bugilta.", oid, varasijat, jonosijaTieto, VARALLA));
            }
            List<HakemuksenTila> hyvaksytytTilat = Arrays.asList(HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY);
            if (TRUE.equals(eiVarasijatayttoa) && !hyvaksytytTilat.contains(jonosijaTieto.tila)) {
                throw new IllegalArgumentException(String.format("Jonolla %s ei ole varasijatäyttöä, mutta sille oltiin asettamassa " +
                    "sivssnovSijoittelunVarasijataytonRajoitus == %s, vaikka rajalla olevan hakemuksen tilan pitäisi olla jokin seuraavista: %s",
                    oid, jonosijaTieto, hyvaksytytTilat));
            }
        });
        this.sivssnovSijoittelunVarasijataytonRajoitus = sivssnovSijoittelunVarasijataytonRajoitus;
    }

    public boolean vapaaVarasijataytto() {
        return !BooleanUtils.isTrue(eiVarasijatayttoa) && !rajoitettuVarasijaTaytto();
    }

    public static class JonosijaTieto {
        public final int jonosija;
        public final int tasasijaJonosija;
        public final HakemuksenTila tila;
        public final List<String> hakemusOidit;

        public JonosijaTieto(int jonosija, int tasasijaJonosija, HakemuksenTila tila, List<String> hakemusOidit) {
            this.jonosija = jonosija;
            this.tasasijaJonosija = tasasijaJonosija;
            this.tila = tila;
            this.hakemusOidit = hakemusOidit;
        }

        public JonosijaTieto(List<Hakemus> hakemuksista) {
            Set<Integer> jonosijat = new HashSet<>();
            Set<HakemuksenTila> tilat = new HashSet<>();
            List<String> hakemusOidit = new LinkedList<>();
            int viimeinenTasasijaJonosija = -1;
            for (Hakemus hakemus : hakemuksista) {
                jonosijat.add(hakemus.getJonosija());
                tilat.add(hakemus.getTila() == VARASIJALTA_HYVAKSYTTY ? HYVAKSYTTY : hakemus.getTila());
                hakemusOidit.add(hakemus.getHakemusOid());
                viimeinenTasasijaJonosija = hakemus.getTasasijaJonosija();
            }
            if (jonosijat.size() != 1) {
                throw new IllegalStateException(String.format("Jonosijatietoa ollaan muodostamassa hakemuksista %s, " +
                    "mutta niistä löytyi yhdestä poikkeava määrä jonosijoja: %s. Vaikuttaa bugilta.", hakemusOidit, jonosijat));
            }
            if (tilat.size() != 1) {
                throw new IllegalStateException(String.format("Jonosijatietoa ollaan muodostamassa hakemuksista %s, " +
                    "mutta niistä löytyi yhdestä poikkeava määrä tiloja: %s. Vaikuttaa bugilta.", hakemusOidit, tilat));
            }
            this.jonosija = jonosijat.iterator().next();
            this.tila = tilat.iterator().next();
            this.tasasijaJonosija = viimeinenTasasijaJonosija;
            this.hakemusOidit = hakemusOidit;

        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return EqualsBuilder.reflectionEquals(this, obj);
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }
}
