package az.bokt.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Точка входа модульного монолита BOKT-next.
 * Сканирование расширено на весь пакет az.bokt, т.к. модули лежат в подпакетах
 * (auth, tenant, client, credit, notification, file-import, audit).
 */
@SpringBootApplication(scanBasePackages = "az.bokt")
@EntityScan(basePackages = "az.bokt")
@EnableJpaRepositories(basePackages = "az.bokt")
public class BoktNextApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoktNextApplication.class, args);
    }
}
