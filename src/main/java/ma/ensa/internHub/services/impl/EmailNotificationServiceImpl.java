package ma.ensa.internHub.services.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.util.HtmlUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.ensa.internHub.domain.dto.request.EmailWithAttachmentsRequest;
import ma.ensa.internHub.exception.EmailSendingException;
import ma.ensa.internHub.services.EmailNotificationService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {
    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String sender;

    @Override
    public void sendHtmlEmail(String to, String subject, String templateName,
            Map<String, Object> templateModel, Map<String, String> inlineResources) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            messageHelper.setFrom(sender);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);

            Context context = new Context();
            if (templateModel != null) {
                templateModel.forEach(context::setVariable);
            }
            String htmlContent = templateEngine.process(templateName, context);
            messageHelper.setText(htmlContent, true);

            // Add inline resources (images, etc.)
            if (inlineResources != null) {
                for (Entry<String, String> resource : inlineResources.entrySet()) {
                    messageHelper.addInline(resource.getKey(),
                            new ClassPathResource(resource.getValue()));
                }
            }

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Failed to send HTML email with template to {}: {}", to, e.getMessage());
            throw new EmailSendingException("Failed to send email to " + to);
        }
    }

    @Override
    public void sendDynamicEmailWithMultipartAttachments(EmailWithAttachmentsRequest emailWithAttachmentsDto) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            messageHelper.setFrom(sender);
            messageHelper.setTo(emailWithAttachmentsDto.getTo());
            messageHelper.setSubject(emailWithAttachmentsDto.getSubject());

            Context context = new Context();
            context.setVariable("recepientName", emailWithAttachmentsDto.getRecepientName());
            context.setVariable("subject", emailWithAttachmentsDto.getSubject());
            context.setVariable("body", emailWithAttachmentsDto.getHtmlBody());

            String htmlContent = templateEngine.process("dynamic-mail", context);
            messageHelper.setText(htmlContent, true);
            MultipartFile[] attachments = emailWithAttachmentsDto.getAttachments();
            if (attachments != null && attachments.length > 0) {
                for (MultipartFile attachment : attachments) {
                    if (!attachment.isEmpty()) {
                        try {
                            String fileName = attachment.getOriginalFilename();
                            Path tempFile = Files.createTempFile(UUID.randomUUID().toString(), fileName);
                            attachment.transferTo(tempFile.toFile());

                            messageHelper.addAttachment(fileName, tempFile.toFile());

                            tempFile.toFile().deleteOnExit();
                        } catch (IOException e) {
                            log.error("Failed to process attachment {}: {}", attachment.getOriginalFilename(),
                                    e.getMessage());
                        }
                    }
                }
            }

            javaMailSender.send(mimeMessage);
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
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(sender);
            helper.setTo(sender);
            helper.setReplyTo(email);
            helper.setSubject("InternHub contact: " + subject.trim());

            String escapedMessage = HtmlUtils.htmlEscape(message.trim())
                    .replace("\r\n", "\n")
                    .replace("\n", "<br>");
            String html = "<h2>New contact form message</h2>"
                    + "<p><strong>From:</strong> " + HtmlUtils.htmlEscape(email.trim()) + "</p>"
                    + "<p><strong>Subject:</strong> " + HtmlUtils.htmlEscape(subject.trim()) + "</p>"
                    + "<p>" + escapedMessage + "</p>";
            helper.setText(html, true);
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            log.error("Failed to send contact message from {}: {}", email, e.getMessage());
            throw new EmailSendingException("Failed to send contact message");
        }
    }
}
