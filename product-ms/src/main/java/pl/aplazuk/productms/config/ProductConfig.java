package pl.aplazuk.productms.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
public class ProductConfig {

    Logger logger = LoggerFactory.getLogger(ProductConfig.class);

    private final ApplicationEventPublisher eventPublisher;

    public ProductConfig(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Scheduled(cron = "0 0/15 * * * ?")
    public void updateConfigServerProperties() {
        RefreshEvent refreshEvent = new RefreshEvent(this, "RefreshEvent", "Refreshing properties from Config Server");
        eventPublisher.publishEvent(refreshEvent);
        logger.info("Config Server properties updated: {}", refreshEvent.getEventDesc());
    }
}
