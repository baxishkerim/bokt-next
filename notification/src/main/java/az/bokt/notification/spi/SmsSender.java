package az.bokt.notification.spi;

/**
 * Порт отправки SMS. Реализацию под конкретного провайдера (шлюз оператора)
 * подключаешь отдельным бином — код приложения от провайдера не зависит.
 * В старом BOKT SMS слался напрямую из процедур; здесь это вынесено за интерфейс.
 */
public interface SmsSender {

    /**
     * Отправить SMS.
     *
     * @param phone   номер получателя в международном формате (+994...)
     * @param text    текст сообщения
     * @return результат отправки
     */
    SmsResult send(String phone, String text);

    record SmsResult(boolean accepted, String providerMessageId, String detail) {
        public static SmsResult ok(String id) {
            return new SmsResult(true, id, null);
        }

        public static SmsResult failed(String detail) {
            return new SmsResult(false, null, detail);
        }
    }
}
