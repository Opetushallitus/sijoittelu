package fi.vm.sade.sijoittelu;

import fi.vm.sade.jetty.OpintopolkuJetty;

public class SijoitteluJetty extends OpintopolkuJetty {
    public static void main(String... args) {
        new SijoitteluJetty().start("/sijoittelu-service");
    }
}
