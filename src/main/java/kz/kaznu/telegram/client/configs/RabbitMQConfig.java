package kz.kaznu.telegram.client.configs;

import lombok.Getter;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Getter
    @Value("${custom.rabbitmq.message.fanout.exchange}")
    private String messageFanoutExchange;

    @Getter
    @Value("${custom.rabbitmq.token.extraction.queue}")
    private String tokenExtractionQueue;

    @Getter
    @Value("${custom.rabbitmq.topic.extraction.queue}")
    private String topicExtractionQueue;

    @Bean
    Queue queue() {
        return new Queue(tokenExtractionQueue, true);
    }

    @Bean
    Queue topicQueue() {
        return new Queue(topicExtractionQueue, true);
    }

    @Bean
    FanoutExchange fanoutExchange() {
        return new FanoutExchange(messageFanoutExchange);
    }

    @Bean
    Binding tokenBinding() {
        return BindingBuilder.bind(queue()).to(fanoutExchange());
    }

    @Bean
    Binding topicBinding() {
        return BindingBuilder.bind(topicQueue()).to(fanoutExchange());
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter();
        jackson2JsonMessageConverter.setCreateMessageIds(true);
        return jackson2JsonMessageConverter;
    }
}
