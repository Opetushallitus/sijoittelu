package fi.vm.sade.sijoittelu.service.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import fi.vm.sade.generic.common.DateHelper;
import fi.vm.sade.tulos.service.TulosService;
import fi.vm.sade.tulos.service.types.HaeHakukohteetKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.HaeHautKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.HaeSijoitteluajotKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakijaTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakijanTilaTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakuTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakukohdeTilaTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakukohdeTyyppi;
import fi.vm.sade.tulos.service.types.tulos.SijoitteluajoTyyppi;
import fi.vm.sade.tulos.service.types.tulos.ValintatapajonoTyyppi;

public class TulosServiceMock implements TulosService {

    private Map<String, HakuTyyppi> hakus = new HashMap<String, HakuTyyppi>();
    private Map<Long, SijoitteluajoTyyppi> sijoitteluajos = new HashMap<Long, SijoitteluajoTyyppi>();
    private Map<Long, List<HakukohdeTyyppi>> sijoitteluajosHakukohdes = new HashMap<Long, List<HakukohdeTyyppi>>();
    private Random random = new Random();

    public TulosServiceMock() {
        createData();
    }

    private void createData() {
        for (int i = 1; i <= 10; ++i) {
            String oid = "hakutyyppioid " + i;

            HakuTyyppi hakuTyyppi = new HakuTyyppi();
            hakuTyyppi.setHaunTunniste("ID " + i);
            hakuTyyppi.setOid(oid);
            hakuTyyppi.getSijoitteluajoIds().addAll(createSijoitteluajos(i, hakuTyyppi));
            hakus.put(oid, hakuTyyppi);
        }
    }

    private List<Long> createSijoitteluajos(int hakuId, HakuTyyppi hakuTyyppi) {
        List<Long> ids = new ArrayList<Long>();
        for (int i = 1; i <= 10; ++i) {
            Long id = new Long(hakuId * 10 + i);

            SijoitteluajoTyyppi sijoitteluajo = new SijoitteluajoTyyppi();
            sijoitteluajo.setSijoitteluId(id);
            sijoitteluajo.setAloitusaika(DateHelper.DateToXmlCal(new Date()));
            sijoitteluajo.setPaattymisaika(DateHelper.DateToXmlCal(new Date()));
            sijoitteluajo.getHakukohdeOids().addAll(createHakukohdes(id, sijoitteluajo));

            sijoitteluajos.put(id, sijoitteluajo);
            ids.add(id);
        }

        return ids;
    }

    private List<String> createHakukohdes(Long id, SijoitteluajoTyyppi sijoitteluajo) {
        List<String> oids = new ArrayList<String>();
        sijoitteluajosHakukohdes.put(id, new ArrayList<HakukohdeTyyppi>());
        for (int i = 1; i <= 10; ++i) {
            Long seq = id * 10 + i;
            String oid = "hakukohdeoid " + seq;
            HakukohdeTyyppi hakukohde = new HakukohdeTyyppi();
            hakukohde.setNimi("hakukohteen nimi " + seq);

            hakukohde.setTila(HakukohdeTilaTyyppi.values()[Math.abs(random.nextInt()) % HakukohdeTilaTyyppi.values().length]);
            hakukohde.setOid(oid);
            hakukohde.getValintatapajonos().addAll(createValintatapajonos(seq, hakukohde));
            sijoitteluajosHakukohdes.get(id).add(hakukohde);
            oids.add(oid);
        }

        return oids;
    }

    private List<ValintatapajonoTyyppi> createValintatapajonos(Long id, HakukohdeTyyppi hakukohde) {
        List<ValintatapajonoTyyppi> oids = new ArrayList<ValintatapajonoTyyppi>();
        for (int i = 1; i <= 3; ++i) {
            Long seq = id * 10 + i;
            String oid = "valintatapajonooid " + seq;
            ValintatapajonoTyyppi valintatapajono = new ValintatapajonoTyyppi();
            valintatapajono.setOid(oid);
            valintatapajono.setPrioriteetti(i);
            valintatapajono.getHakijaList().addAll(createHakijas());
            oids.add(valintatapajono);
        }
        return oids;
    }

    private List<HakijaTyyppi> createHakijas() {
        List<HakijaTyyppi> hakijas = new ArrayList<HakijaTyyppi>();
        for (int i = 1; i < 10; ++i) {
            HakijaTyyppi hakija = new HakijaTyyppi();
            hakija.setJonosija(i);
            hakija.setOid("hakijaoid " + i);
            hakija.setPisteet(Math.abs(random.nextInt()) % 100 + 1);
            hakija.setPrioriteetti(Math.abs(random.nextInt()) % 10 + 1);
            hakija.setTila(HakijanTilaTyyppi.values()[Math.abs(random.nextInt()) % HakijanTilaTyyppi.values().length]);
            hakijas.add(hakija);
        }

        return hakijas;
    }

    @Override
    public List<SijoitteluajoTyyppi> haeSijoitteluajot(HaeSijoitteluajotKriteeritTyyppi haeSijoitteluajotKriteerit) {
        List<SijoitteluajoTyyppi> result = new ArrayList<SijoitteluajoTyyppi>();
        if (haeSijoitteluajotKriteerit == null) {
            result.addAll(sijoitteluajos.values());
        } else {
            for (Long sijoitteluId : haeSijoitteluajotKriteerit.getSijoitteluIdLista()) {
                result.add(sijoitteluajos.get(sijoitteluId));
            }
        }

        return result;
    }

    @Override
    public List<HakuTyyppi> haeHaut(HaeHautKriteeritTyyppi haeHautKriteerit) {
        List<HakuTyyppi> result = new ArrayList<HakuTyyppi>();
        if (haeHautKriteerit == null) {
            result.addAll(hakus.values());
        } else {
            for (String hakuOid : haeHautKriteerit.getHakuOidLista()) {
                result.add(hakus.get(hakuOid));
            }
        }

        return result;
    }

    @Override
    public List<HakukohdeTyyppi> haeHakukohteet(long sijoitteluId, HaeHakukohteetKriteeritTyyppi haeHakukohteetKriteerit) {
        List<HakukohdeTyyppi> result = new ArrayList<HakukohdeTyyppi>();
        if (haeHakukohteetKriteerit == null) {
            result.addAll(sijoitteluajosHakukohdes.get(sijoitteluId));
        } else {
            for (HakukohdeTyyppi h : sijoitteluajosHakukohdes.get(sijoitteluId)) {
                for (String oid : haeHakukohteetKriteerit.getHakukohdeOidLista()) {
                    if (h.getOid().equals(oid)) {
                        result.add(h);
                    }
                }
            }
        }

        return result;
    }
}
