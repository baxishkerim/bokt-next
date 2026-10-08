package az.bokt.credit.processing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProcessingConfig {

    /** Заглушка процессинга — пока не подключён реальный ProcessingClient. */
    @Bean
    @ConditionalOnMissingBean(ProcessingClient.class)
    public ProcessingClient stubProcessingClient() {
        return new StubProcessingClient();
    }
}
