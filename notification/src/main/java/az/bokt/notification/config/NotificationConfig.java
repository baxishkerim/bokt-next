package az.bokt.notification.config;

import az.bokt.notification.spi.SmsSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {

    /** Заглушка SMS — используется, пока не подключён реальный SmsSender. */
    @Bean
    @ConditionalOnMissingBean(SmsSender.class)
    public SmsSender loggingSmsSender() {
        return new LoggingSmsSender();
    }
}
