package pl.aplazuk.orderms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClient;

@Configuration
public class OrderConfig {

    Logger logger = LoggerFactory.getLogger(OrderConfig.class);

    private final ApplicationEventPublisher eventPublisher;

    public OrderConfig(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Bean
    public RestClient.Builder getRestClientBuilder() {
        return RestClient.builder();
    }

    @Scheduled(cron = "0 0/15 * * * ?")
    public void updateConfigServerProperties() {
        RefreshEvent refreshEvent = new RefreshEvent(this, "RefreshEvent", "Refreshing properties from Config Server");
        eventPublisher.publishEvent(refreshEvent);
        logger.info("Config Server properties updated: {}", refreshEvent.getEventDesc());
    }
}
