package com.reskill.messaging.listener;

import com.reskill.messaging.config.RabbitConfig;
import com.reskill.dto.EmailMessage;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailListener {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitConfig.QUEUE_EMAIL)
    public void receiveEmail(EmailMessage message) {
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, "UTF-8");


            helper.setFrom("rafaelmacoto@gmail.com");

            helper.setTo(message.to());
            helper.setSubject(message.subject());
            helper.setText(message.body(), true);

            mailSender.send(mime);

            log.info("Email enviado para {}", message.to());

        } catch (Exception e) {
            log.error("Erro ao enviar email: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
