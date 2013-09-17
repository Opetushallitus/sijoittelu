package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

public class EnumConverter {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <T extends Enum> T convert(Class<T> e1, Enum<?> e2) {
        return Enum.<T> valueOf(e1, e2.toString());
    }
}
