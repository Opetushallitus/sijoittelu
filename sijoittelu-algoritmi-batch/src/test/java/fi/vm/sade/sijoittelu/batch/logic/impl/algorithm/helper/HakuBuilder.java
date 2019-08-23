package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper;


import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;

public class HakuBuilder {
    public static class SijoitteluajoWrapperBuilder {
        private final SijoitteluajoWrapper wrapper;

        public SijoitteluajoWrapperBuilder(List<Hakukohde> hakukohteet) {
            this(hakukohteet, "test_haku");
        }

        public SijoitteluajoWrapperBuilder(List<Hakukohde> hakukohteet, String hakuOid) {
            SijoitteluAjo sa = new SijoitteluAjo();
            sa.setHakuOid(hakuOid);
            this.wrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                new SijoitteluConfiguration(), sa, hakukohteet, new ArrayList<>(), Collections.emptyMap());
            this.wrapper.paivitaVastaanottojenVaikutusHakemustenTiloihin(Collections.emptyList(), Collections.emptyMap());
        }

        public SijoitteluajoWrapperBuilder withVarasijaSaannotAstuvatVoimaan(LocalDateTime varasijaSaannotAstuvatVoimaan) {
            wrapper.setVarasijaSaannotAstuvatVoimaan(varasijaSaannotAstuvatVoimaan);
            return this;
        }

        public SijoitteluajoWrapperBuilder withKKHaku(boolean kkHaku) {
            wrapper.setKKHaku(kkHaku);
            return this;
        }

        public SijoitteluajoWrapper build() { return wrapper; }

        public SijoitteluajoWrapperBuilder withAmkopeHaku(boolean b) {
            wrapper.setAmkopeHaku(b);
            return this;
        }
    }

    public static class HakukohdeBuilder {
        private final Hakukohde hk;

        public HakukohdeBuilder(String oid) {
            this.hk = new Hakukohde();
            this.hk.setOid(oid);
        }

        public Hakukohde build() {
            return hk;
        }

        public HakukohdeBuilder withValintatapajono(Valintatapajono a) {
            hk.getValintatapajonot().add(a);
            return this;
        }
    }

    public static class ValintatapajonoBuilder {
        private final Valintatapajono vtj;

        public ValintatapajonoBuilder() {
            this.vtj = new Valintatapajono();
        }

        public ValintatapajonoBuilder withOid(String a) {
            vtj.setOid(a);
            return this;
        }

        public ValintatapajonoBuilder withTayttojono(String a) {
            vtj.setTayttojono(a);
            return this;
        }

        public ValintatapajonoBuilder withAloituspaikat(int a) {
            vtj.setAloituspaikat(a);
            return this;
        }

        public ValintatapajonoBuilder withHakemus(Hakemus h) {
            this.vtj.getHakemukset().add(h);
            return this;
        }

        public ValintatapajonoBuilder withHakemukset(Hakemus... hakemukset) {
            ValintatapajonoBuilder returnValue = this;
            for (Hakemus hakemus : hakemukset) {
                returnValue = withHakemus(hakemus);
            }
            return returnValue;
        }

        public ValintatapajonoBuilder withHakemus(HakemuksenTila a) {
            Hakemus h = new Hakemus();
            h.setHakemusOid("oid");
            TilojenMuokkaus.asetaTilaksiVaralla(h);
            h.setEdellinenTila(a);
            vtj.getHakemukset().add(h);
            return this;
        }

        public ValintatapajonoBuilder withHakemus(HakemuksenTila a, String oid) {
            this.withHakemus(a).withOid(oid);
            return this;
        }

        public ValintatapajonoBuilder withSivssnov(boolean sivssnov) {
            vtj.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(sivssnov);
            return this;
        }

        public ValintatapajonoBuilder withPrioriteetti(int prioriteetti) {
            vtj.setPrioriteetti(prioriteetti);
            return this;
        }

        public ValintatapajonoBuilder withTasasijasaanto(Tasasijasaanto tasasijasaanto) {
            vtj.setTasasijasaanto(tasasijasaanto);
            return this;
        }

        public Valintatapajono build() {
            setSafeDefaultsForMissingValues();
            return vtj;
        }

        void setSafeDefaultsForMissingValues() {
            if (vtj.getKaikkiEhdonTayttavatHyvaksytaan() == null) {
                vtj.setKaikkiEhdonTayttavatHyvaksytaan(false);
            }
        }
    }

    public static class HakemusBuilder {
        private final Hakemus h;

        public HakemusBuilder() {
            this.h = new Hakemus();
        }

        public HakemusBuilder withOid(String val) {
            this.h.setHakemusOid(val);
            return this;
        }

        public HakemusBuilder withTila(HakemuksenTila val) {
            this.h.setTila(val);
            return this;
        }

        public HakemusBuilder withTilankuvauksenTarkenne(TilankuvauksenTarkenne val) {
            this.h.setTilankuvauksenTarkenne(val);
            return this;
        }

        public HakemusBuilder withEdellinenTila(HakemuksenTila val) {
            this.h.setEdellinenTila(val);
            return this;
        }

        public HakemusBuilder withHakijaOid(String val) {
            this.h.setHakijaOid(val);
            return this;
        }

        public HakemusBuilder withPrioriteetti(int val) {
            this.h.setPrioriteetti(val);
            return this;
        }

        public HakemusBuilder withJonosija(int jonosija) {
            this.h.setJonosija(jonosija);
            return this;
        }

        public HakemusBuilder withTasasijaJonosija(int tasasijaJonosija) {
            this.h.setTasasijaJonosija(tasasijaJonosija);
            return this;
        }

        public Hakemus build() {
            return this.h;
        }
    }
}
