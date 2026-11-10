package ma.ensa.internHub.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensa.internHub.domain.dto.request.ContactMessageRequest;
import ma.ensa.internHub.services.EmailNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
public class ContactController {
    private final EmailNotificationService emailNotificationService;

    @PostMapping
    public ResponseEntity<Void> sendContactMessage(@Valid @RequestBody ContactMessageRequest request) {
        emailNotificationService.sendContactMessage(
                request.getEmail(), request.getSubject(), request.getMessage());
        return ResponseEntity.ok().build();
    }
}
