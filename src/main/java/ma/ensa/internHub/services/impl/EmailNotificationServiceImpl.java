package ma.ensa.internHub.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.HtmlUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.InputStreamSource;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import ma.ensa.internHub.domain.dto.request.EmailWithAttachmentsRequest;
import ma.ensa.internHub.exception.EmailSendingException;
import ma.ensa.internHub.services.EmailNotificationService;

@Service
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final RestTemplate restTemplate;

    public EmailNotificationServiceImpl(
            ObjectProvider<JavaMailSender> mailSenderProvider,
            SpringTemplateEngine templateEngine,
            RestTemplateBuilder restTemplateBuilder) {
        this.javaMailSender = mailSenderProvider.getIfAvailable();
        this.templateEngine = templateEngine;
        this.restTemplate = restTemplateBuilder.build();
    }

    @Value("${RESEND_API_KEY:}")
    private String resendApiKey;

    @Value("${MAIL_FROM:${spring.mail.username}}")
    private String sender;

    @Override
    public void sendHtmlEmail(String to, String subject, String templateName,
            Map<String, Object> templateModel, Map<String, String> inlineResources) {
        try {
            Context context = new Context();
            if (templateModel != null) {
                templateModel.forEach(context::setVariable);
            }
            String htmlContent = templateEngine.process(templateName, context);
            List<ResendAttachment> inlineAttachments = new ArrayList<>();
            if (inlineResources != null) {
                for (Entry<String, String> resource : inlineResources.entrySet()) {
                    ClassPathResource image = new ClassPathResource(resource.getValue());
                    String mediaType = Files.probeContentType(Path.of(resource.getValue()));
                    inlineAttachments.add(new ResendAttachment(resource.getKey(),
                            Base64.getEncoder().encodeToString(image.getInputStream().readAllBytes()),
                            mediaType == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : mediaType,
                            "inline", resource.getKey()));
                }
            }
            sendWithFallback(to, subject, htmlContent, null, inlineAttachments);
        } catch (Exception e) {
            log.error("Failed to send HTML email with template to {}: {}", to, e.getMessage());
            throw new EmailSendingException("Failed to send email to " + to);
        }
    }

    @Override
    public void sendDynamicEmailWithMultipartAttachments(EmailWithAttachmentsRequest emailWithAttachmentsDto) {
        try {
            Context context = new Context();
            context.setVariable("recepientName", emailWithAttachmentsDto.getRecepientName());
            context.setVariable("subject", emailWithAttachmentsDto.getSubject());
            context.setVariable("body", emailWithAttachmentsDto.getHtmlBody());

            String htmlContent = templateEngine.process("dynamic-mail", context);
            List<ResendAttachment> emailAttachments = new ArrayList<>();
            MultipartFile[] uploadedAttachments = emailWithAttachmentsDto.getAttachments();
            if (uploadedAttachments != null && uploadedAttachments.length > 0) {
                for (MultipartFile attachment : uploadedAttachments) {
                    if (!attachment.isEmpty()) {
                        try {
                            emailAttachments.add(new ResendAttachment(attachment.getOriginalFilename(),
                                    Base64.getEncoder().encodeToString(attachment.getBytes()),
                                    attachment.getContentType(), null, null));
                        } catch (IOException e) {
                            throw new IllegalStateException("Failed to read email attachment "
                                    + attachment.getOriginalFilename(), e);
                        }
                    }
                }
            }

            sendWithFallback(emailWithAttachmentsDto.getTo(), emailWithAttachmentsDto.getSubject(),
                    htmlContent, null, emailAttachments);
        } catch (Exception e) {
            log.error("Failed to send dynamic HTML email with attachments to {}: {}", emailWithAttachmentsDto.getTo(),
                    e.getMessage());
            throw new EmailSendingException(
                    "Failed to send dynamic email with attachments to " + emailWithAttachmentsDto.getTo());
        }
    }

    @Override
    public void sendEmailConfirmationCode(String email, String username, String verificationCode) {
        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("userName", username);
        templateModel.put("confirmationCode", verificationCode);

        sendHtmlEmail(
                email,
                "InternHub - Email Confirmation Code",
                "confirmation-code",
                templateModel,
                null);
    }

    @Override
    public void sendContactMessage(String email, String subject, String message) {
        try {
            String escapedMessage = HtmlUtils.htmlEscape(message.trim())
                    .replace("\r\n", "\n")
                    .replace("\n", "<br>");
            String html = "<h2>New contact form message</h2>"
                    + "<p><strong>From:</strong> " + HtmlUtils.htmlEscape(email.trim()) + "</p>"
                    + "<p><strong>Subject:</strong> " + HtmlUtils.htmlEscape(subject.trim()) + "</p>"
                    + "<p>" + escapedMessage + "</p>";
            sendWithFallback(sender, "InternHub contact: " + subject.trim(), html, email, List.of());
        } catch (Exception e) {
            log.error("Failed to send contact message from {}: {}", email, e.getMessage());
            throw new EmailSendingException("Failed to send contact message");
        }
    }

    private void sendWithFallback(String to, String subject, String html, String replyTo,
            List<ResendAttachment> attachments) throws Exception {
        if (resendApiKey != null && !resendApiKey.isBlank()) {
            try {
                sendViaResend(to, subject, html, replyTo, attachments);
                return;
            } catch (RestClientException | IOException e) {
                log.warn("Resend API send failed; trying SMTP fallback: {}", e.getMessage());
            }
        }

        if (javaMailSender == null) {
            throw new IllegalStateException("SMTP fallback is not configured and Resend could not send the email");
        }
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(sender);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);
        if (replyTo != null && !replyTo.isBlank()) {
            helper.setReplyTo(replyTo);
        }
        for (ResendAttachment attachment : attachments) {
            byte[] data = Base64.getDecoder().decode(attachment.content);
            InputStreamSource source = new ByteArrayResource(data) {
                @Override
                public String getFilename() {
                    return attachment.filename;
                }
            };
            if (attachment.disposition != null && "inline".equals(attachment.disposition)) {
                helper.addInline(attachment.contentId, source, attachment.contentType);
            } else {
                helper.addAttachment(attachment.filename, source);
            }
        }
        javaMailSender.send(message);
    }

    private void sendViaResend(String to, String subject, String html, String replyTo,
            List<ResendAttachment> attachments) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("from", sender);
        payload.put("to", List.of(to));
        payload.put("subject", subject);
        payload.put("html", html);
        if (replyTo != null && !replyTo.isBlank()) {
            payload.put("reply_to", replyTo);
        }
        if (!attachments.isEmpty()) {
            List<Map<String, String>> resendAttachments = attachments.stream().map(attachment -> {
                Map<String, String> item = new HashMap<>();
                item.put("filename", attachment.filename);
                item.put("content", attachment.content);
                if (attachment.contentType != null && !attachment.contentType.isBlank()) {
                    item.put("content_type", attachment.contentType);
                }
                if (attachment.contentId != null && !attachment.contentId.isBlank()) {
                    item.put("content_id", attachment.contentId);
                }
                return item;
            }).toList();
            payload.put("attachments", resendAttachments);
        }
        RequestEntity<Map<String, Object>> request = RequestEntity.post("https://api.resend.com/emails")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + resendApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload);
        restTemplate.exchange(request, String.class);
    }

    private static class ResendAttachment {
        public final String filename;
        public final String content;
        @com.fasterxml.jackson.annotation.JsonProperty("content_type")
        public final String contentType;
        @com.fasterxml.jackson.annotation.JsonProperty("content_disposition")
        public final String disposition;
        @com.fasterxml.jackson.annotation.JsonProperty("content_id")
        public final String contentId;

        ResendAttachment(String filename, String content, String contentType, String disposition, String contentId) {
            this.filename = filename;
            this.content = content;
            this.contentType = contentType;
            this.disposition = disposition;
            this.contentId = contentId;
        }
    }
}
