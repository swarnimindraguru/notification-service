package com.swarnim.notificationservice.configs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swarnim.notificationservice.dtos.SendEmailMessageDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

@Configuration
public class KafkaConsumerConfig {
    //Code to listen an event from Kafka should come

    @Value("${sender-email.key.id}")
    private String senderEmailKeyId;

    @Value("${sender-email.key.secret}")
    private String senderEmailKeySecret;

    private ObjectMapper objectMapper;
    private EmailUtil emailUtil;

    public KafkaConsumerConfig(ObjectMapper objectMapper, EmailUtil emailUtil){
        this.objectMapper=objectMapper;
        this.emailUtil = emailUtil;
    }

    //here we have used groupId - so that this handleSendEmailEvent method should be executed by only one server in case if there are multiple NotificationService servers
    @KafkaListener(topics = "sendEmail", groupId = "notificationServer")
    public void handleSendEmailEvent(String message) throws JsonProcessingException {
        //Code to send Email to the user
        SendEmailMessageDto messageDto = objectMapper.readValue(message,SendEmailMessageDto.class);
        System.out.println("Got req in handleSendEmailEvent");

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com"); //SMTP Host
        props.put("mail.smtp.port", "587"); //TLS Port
        props.put("mail.smtp.auth", "true"); //enable authentication
        props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmailKeyId, senderEmailKeySecret);
            }
        };
        Session session = Session.getInstance(props, auth);

        EmailUtil.sendEmail(session, messageDto.getTo(), messageDto.getSubject(), messageDto.getBody());
    }
}
