package com.reskill.messaging.producer;

import com.reskill.messaging.config.RabbitConfig;
import com.reskill.dto.EmailMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailProducer {

    private final AmqpTemplate amqpTemplate;

    public void sendEmail(EmailMessage email) {
        amqpTemplate.convertAndSend(
                RabbitConfig.EXCHANGE,
                RabbitConfig.ROUTING_EMAIL,
                email
        );
    }
}
