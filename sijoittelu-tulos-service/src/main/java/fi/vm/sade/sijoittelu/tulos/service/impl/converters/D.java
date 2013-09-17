package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;

public class D {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        System.out.println(EnumConverter.convert(HakemuksenTila.class,
                fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYVAKSYTTY));

    }

}
