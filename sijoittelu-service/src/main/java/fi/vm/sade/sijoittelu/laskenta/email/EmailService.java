package fi.vm.sade.sijoittelu.laskenta.email;

import fi.vm.sade.sijoittelu.laskenta.service.it.Haku;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.api.email.Recipient;
import org.simplejavamail.api.mailer.Mailer;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;
import org.simplejavamail.mailer.internal.MailerRegularBuilderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.Message;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailService {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private static final String SENDER_NAME = "Opintopolun sijoittelu";
    private static final String SENDER_EMAIL_ADDRESS = "noreply@opintopolku.fi";

    private static final String ERROR_YHTEISHAKU_SUBJECT = "Yhteishaun sijoitteluajo epäonnistui!";
    private static final String ERROR_YHTEISHAKU_TOINEN_ASTE_MESSAGE = "2. asteen yhteishaun sijoitteluajo epäonnistui!";
    private static final String ERROR_YHTEISHAKU_KK_MESSAGE = "Korkeakoulujen yhteishaun sijoitteluajo epäonnistui!";

    private final boolean sendingEnabled;
    private final String toinenAsteEmails;
    private final String kkEmails;

    private Mailer mailer;

    public EmailService(
            @Value("${sijoittelu.email.enabled:false}") boolean sendingEnabled,
            @Value("${sijoittelu.email.smtp.host:invalid.domain}") String smtpHost,
            @Value("${sijoittelu.email.smtp.port:25}") int smtpPort,
            @Value("${sijoittelu.email.smtp.username:}") String smtpUsername,
            @Value("${sijoittelu.email.smtp.password:}") String smtpPassword,
            @Value("${sijoittelu.email.smtp.use_tls:false}") boolean useTLS,
            @Value("${sijoittelu.email.smtp.toinen_aste_emails:}") String toinenAsteEmails,
            @Value("${sijoittelu.email.smtp.kk_emails:}") String kkEmails,
            @Value("${sijoittelu.email.smtp.use_authentication:false}") boolean useAuthentication
    ) {
        LOGGER.info("Configuration: {" +
            "sendingEnabled=" + sendingEnabled +
                    ", smtpHost='" + smtpHost + '\'' +
                    ", smtpPort=" + smtpPort +
                    ", smtpUsername='" + smtpUsername + '\'' +
                    ", smtpPassword='" + "*".repeat(smtpPassword.length()) + '\'' +
                    ", useTLS=" + useTLS +
                    ", toinenAsteEmails='" + toinenAsteEmails + '\'' +
                    ", kkEmails='" + kkEmails + '\'' +
                    '}');
        MailerRegularBuilderImpl builder = MailerBuilder
                .withSMTPServerHost(smtpHost)
                .withSMTPServerPort(smtpPort)
                .withTransportStrategy(useTLS ? TransportStrategy.SMTP_TLS : TransportStrategy.SMTP)
                .withSessionTimeout(10 * 1000)
                .async();
        if(useAuthentication) {
            builder
                .withSMTPServerUsername(smtpUsername)
                .withSMTPServerPassword(smtpPassword);
        }
        mailer = builder.buildMailer();

        this.sendingEnabled = sendingEnabled;
        this.toinenAsteEmails = toinenAsteEmails;
        this.kkEmails = kkEmails;
    }

    public void sendToinenAsteErrorEmail(Haku haku, Throwable t) {
        sendErrorEmail(getRecipients(toinenAsteEmails), createEmailBody(haku, t, ERROR_YHTEISHAKU_TOINEN_ASTE_MESSAGE));
    }

    public void sendKkErrorEmail(Haku haku, Throwable t) {
        sendErrorEmail(getRecipients(kkEmails), createEmailBody(haku, t, ERROR_YHTEISHAKU_KK_MESSAGE));
    }

    private void sendErrorEmail(List<Recipient> recipients, String body) {
        Email email = EmailBuilder.startingBlank()
                .withRecipients(recipients)
                .from(SENDER_NAME, SENDER_EMAIL_ADDRESS)
                .withHeader("Content-Transfer-Encoding", "base64")
                .withHeader("Encoding", "base64")
                .withSubject(ERROR_YHTEISHAKU_SUBJECT)
                .withHTMLText(body)
                .buildEmail();

        if(sendingEnabled) {
            LOGGER.info("Sending error email: {}", email);
            mailer.sendMail(email);
        } else {
            LOGGER.info("Not sending error email: {}", email);
        }
    }

    private List<Recipient> getRecipients(String addressString) {
        String[] addresses = addressString.split(";");
        List<Recipient> recipients = Arrays.stream(addresses)
                .map(address -> address.trim())
                .map(address -> new Recipient(address, address, Message.RecipientType.TO))
                .collect(Collectors.toList());
        return recipients;
    }

    private String createEmailBody(Haku haku, Throwable t, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("<meta charset=\"UTF-8\">")
        .append("<html>")
        .append("<body>")
        .append("<h2>").append(message).append("</h2>")
        .append("<p>Haku: <pre>").append(haku.oid).append("</pre></p>")
        .append("<p>Virhe: <pre>").append(ExceptionUtils.getMessage(t)).append("</pre></p>")
        .append("<p><pre>").append(ExceptionUtils.getStackTrace(t)).append("</pre></p>")
        .append("</body>")
        .append("</html>");
        LOGGER.info("Email message: {}", sb);
        return sb.toString();
    }

}
