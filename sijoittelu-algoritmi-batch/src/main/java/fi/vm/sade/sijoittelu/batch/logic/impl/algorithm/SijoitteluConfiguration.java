package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SijoitteluConfiguration {
    public final boolean kaytaVtku31SaantoaRajoitetussaVarasijataytossa;

    @Autowired
    public SijoitteluConfiguration(@Value("${sijoittelu.kayta.vtku31.saantoa.rajatussa.varasijataytossa:false}")
                                           boolean kaytaVtku31SaantoaRajoitetussaVarasijataytossa) {
        this.kaytaVtku31SaantoaRajoitetussaVarasijataytossa = kaytaVtku31SaantoaRajoitetussaVarasijataytossa;
    }

    public SijoitteluConfiguration() {
        this(false);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
