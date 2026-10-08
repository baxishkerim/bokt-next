package az.bokt.notification.service;

import az.bokt.notification.spi.SmsSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Прикладные уведомления. Шаблоны собраны здесь, чтобы не разбрасывать текст SMS по коду,
 * как это было в BOKT (текст логина/пароля хардкодился прямо в инструкции поддержки).
 */
@Service
public class NotificationService {

    private final SmsSender smsSender;

    public NotificationService(SmsSender smsSender) {
        this.smsSender = smsSender;
    }

    /** Отправка учётных данных новому пользователю (аналог SMS из раздела "Creating a new User"). */
    public void sendCredentials(String phone, String username, String password, String otp) {
        String text = """
                Salam.
                Sizin ani kredit sistemində hesab məlumatlarınız:
                Username: %s
                Parol: %s
                OTP: %s""".formatted(username, password, otp);
        smsSender.send(phone, text);
    }

    /** Одноразовый код для входа. */
    public void sendLoginOtp(String phone, String otp, int ttlMinutes) {
        String text = "Ani kredit sistemi. Giriş kodu: %s. Kod %d dəqiqə ərzində etibarlıdır."
                .formatted(otp, ttlMinutes);
        smsSender.send(phone, text);
    }

    /** Уведомление клиента о выданном кредите (если у NBCO включён SMS-сервис). */
    public void sendCreditIssued(String phone, BigDecimal amount, String currency, String secretWord) {
        String text = ("Ani kredit sistemi. Kartınıza %s %s köçürüldü. "
                + "Çağrı mərkəzi ilə əlaqə üçün gizli söz: %s")
                .formatted(amount.toPlainString(), currency, secretWord);
        smsSender.send(phone, text);
    }
}
