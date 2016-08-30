package fi.vm.sade.sijoittelu.tulos.mongo;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;

import java.io.IOException;

public class MongodV3ForTestsFactory extends MongodForTestsFactory {
    public MongodV3ForTestsFactory() throws IOException {
        super(Version.Main.V2_4);
    }
}
