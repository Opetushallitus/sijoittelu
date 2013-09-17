package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         Geneerinen enum convertteri
 */
public class EnumConverter {
    public static <T extends Enum<T>> T convert(Class<T> e1, Enum<?> e2) {
        if (e2 == null) {
            return null;
        }
        return Enum.valueOf(e1, e2.toString());
    }
}
