package fi.vm.sade.sijoittelu.laskenta.util;

import fi.vm.sade.auditlog.ApplicationType;
import fi.vm.sade.auditlog.Audit;
import org.springframework.security.core.context.SecurityContextHolder;

import java.security.Principal;
import java.util.Optional;

/**
 * @author Jussi Jartamo
 */
public class SijoitteluAudit {
    public static final Audit AUDIT = new Audit("sijoittelu-service", ApplicationType.VIRKAILIJA);

    public static String username() {
        return Optional.ofNullable((Principal) SecurityContextHolder.getContext().getAuthentication()).orElse(
                () -> "Kirjautumaton käyttäjä"
        ).getName();
    }
}
