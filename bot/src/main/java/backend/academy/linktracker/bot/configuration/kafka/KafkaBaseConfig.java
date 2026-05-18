package backend.academy.linktracker.bot.configuration.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
@ConditionalOnProperty(name = "app.client.scrapper.api.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaBaseConfig {}
