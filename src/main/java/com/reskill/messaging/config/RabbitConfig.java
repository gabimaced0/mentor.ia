package com.reskill.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "reskill.direct.exchange";
    public static final String QUEUE_EMAIL = "reskill.email.queue";
    public static final String ROUTING_EMAIL = "reskill.email.send";

    @Bean
    public DirectExchange reskillExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(QUEUE_EMAIL, true);
    }

    @Bean
    public Binding bindingEmail(Queue emailQueue, DirectExchange reskillExchange) {
        return BindingBuilder.bind(emailQueue)
                .to(reskillExchange)
                .with(ROUTING_EMAIL);
    }
}
