package az.bokt.notification.config;

import az.bokt.notification.spi.SmsSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Заглушка на время разработки: печатает SMS в лог. Регистрируется как бин в
 * {@link NotificationConfig} с @ConditionalOnMissingBean — отключится, когда появится
 * реальная реализация {@link SmsSender}.
 */
public class LoggingSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

    @Override
    public SmsResult send(String phone, String text) {
        String id = UUID.randomUUID().toString();
        log.info("[SMS-STUB] -> {} | id={} | text=\n{}", mask(phone), id, text);
        return SmsResult.ok(id);
    }

    private String mask(String phone) {
        if (phone == null || phone.length() < 4) {
            return "***";
        }
        return "***" + phone.substring(phone.length() - 4);
    }
}
