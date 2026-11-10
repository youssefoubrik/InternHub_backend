package ma.ensa.internHub.services;

import java.util.Map;

import ma.ensa.internHub.domain.dto.request.EmailWithAttachmentsRequest;

public interface EmailNotificationService {

        void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateModel,
                        Map<String, String> inlineResources);

        void sendDynamicEmailWithMultipartAttachments(EmailWithAttachmentsRequest emailWithAttachmentsDto);

        void sendEmailConfirmationCode(String email, String username, String verificationCode);

        void sendContactMessage(String email, String subject, String message);

}
