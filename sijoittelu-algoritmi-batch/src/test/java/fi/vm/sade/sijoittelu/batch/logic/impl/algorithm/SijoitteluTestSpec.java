package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Jussi Jartamo
 */
public abstract class SijoitteluTestSpec {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluTestSpec.class);

    public Function<HakuDTO,SijoitteluajoWrapper> algoritmi() {
        return (haku) -> {
            List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
            SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
            SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.<Valintatulos>newArrayList());
            s.start();
            LOG.debug("\r\n{}", PrintHelper.tulostaSijoittelu(s));
            return s.getSijoitteluAjo();
        };
    }

    public SijoitteluajoWrapper kaksiJonoaJaHakijaryhmaKolmenKiintiolla() {
        HakuDTO t = TestHelper.xmlToObjects("testdata_erikoistapaukset/sijoittelu_2jonoa_ja_hakijaryhma_3kiintiolla.json");
        return algoritmi().apply(t);
    }

    public SijoitteluajoWrapper kaksiHakukohdettaJaKaksiJonoaJaHakijaryhmaYhdenKiintiolla() {
        HakuDTO t = TestHelper.xmlToObjects("testdata_erikoistapaukset/sijoittelu_2hakukohdetta_2jonoa_ja_hakijaryhma_1kiintiolla.json");
        return algoritmi().apply(t);
    }
}
