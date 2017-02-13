package fi.vm.sade.sijoittelu.laskenta.util;

import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO;

import java.util.Optional;

public class HakuUtil {

    public static Optional<String> getHaunKohdejoukko(HakuDTO hakuDto) {
        return extractKoodiUri(hakuDto.getKohdejoukkoUri());
    }

    public static Optional<String> gethaunKohdejoukonTarkenne(HakuDTO hakuDto) {
        return extractKoodiUri(hakuDto.getKohdejoukonTarkenne());
    }

    private static Optional<String> extractKoodiUri(String koodi) {
        if (koodi == null) {
            return Optional.empty();
        }
        return Optional.of(koodi.split("#")[0]);
    }
}
