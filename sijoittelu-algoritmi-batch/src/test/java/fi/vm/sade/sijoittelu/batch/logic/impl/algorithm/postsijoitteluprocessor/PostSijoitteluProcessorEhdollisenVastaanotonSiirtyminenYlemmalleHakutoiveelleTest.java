package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactoryImpl;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelleTest {
    List<Hakukohde> hakukohdeList = TestHelper.readHakukohteetListFromJson("testdata/sijoittelu_hakukohde.json");
    List<Valintatulos> valintatulosList = TestHelper.readValintatulosListFromJson("testdata/sijoittelu_valintatulos.json");
    final PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelle postProcessor =
            new PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelle();

    @Test
    public void testEhdollisenVastaanotonSiirtyminenYlospain() {
        SijoitteluAlgorithmFactoryImpl factory = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm algorithm = factory.constructAlgorithm(hakukohdeList, valintatulosList);
        final SijoitteluajoWrapper sijoitteluAjo = algorithm.getSijoitteluAjo();
        sijoitteluAjo.getMuuttuneetValintatulokset().addAll(valintatulosList);
        postProcessor.process(sijoitteluAjo);
    }
}
