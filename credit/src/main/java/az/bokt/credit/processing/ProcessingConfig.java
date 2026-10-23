package az.bokt.credit.processing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TranzaxisProperties.class)
public class ProcessingConfig {

    /**
     * Заглушка процессинга — активна, пока TranzAxis выключен
     * (bokt.processing.tranzaxis.enabled отсутствует или =false).
     * При enabled=true создаётся {@link TranzaxisProcessingClient}, а этот бин не поднимается.
     */
    @Bean
    @ConditionalOnProperty(prefix = "bokt.processing.tranzaxis", name = "enabled",
            havingValue = "false", matchIfMissing = true)
    public ProcessingClient stubProcessingClient() {
        return new StubProcessingClient();
    }
}
