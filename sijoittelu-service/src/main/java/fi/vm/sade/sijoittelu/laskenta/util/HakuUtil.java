package fi.vm.sade.sijoittelu.laskenta.util;

import fi.vm.sade.sijoittelu.laskenta.service.it.Haku;

import java.util.Optional;

public class HakuUtil {

    public static Optional<String> getHaunKohdejoukko(Haku haku) {
        return extractKoodiUri(haku.haunkohdejoukkoUri);
    }

    public static Optional<String> gethaunKohdejoukonTarkenne(Haku haku) {
        return extractKoodiUri(haku.haunkohdejoukontarkenneUri);
    }

    private static Optional<String> extractKoodiUri(String koodi) {
        if (koodi == null) {
            return Optional.empty();
        }
        return Optional.of(koodi.split("#")[0]);
    }
}
