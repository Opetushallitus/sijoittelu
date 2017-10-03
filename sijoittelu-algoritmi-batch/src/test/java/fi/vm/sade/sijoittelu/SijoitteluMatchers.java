package fi.vm.sade.sijoittelu;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class SijoitteluMatchers {
    public static BaseMatcher<Hakemus> hasTila(final HakemuksenTila expectedTila) {
        return new BaseMatcher<Hakemus>() {
            @Override
            public boolean matches(Object item) {
                return expectedTila.equals(((Hakemus) item).getTila());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("hakemus with tila " + expectedTila);
            }
        };
    }
}
