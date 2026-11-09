package ma.ensa.internHub.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import ma.ensa.internHub.domain.dto.response.StorageUploadResponse;
import ma.ensa.internHub.services.SupabaseStorageService;

@RestController
@RequestMapping("/api/v1/storage/images")
@RequiredArgsConstructor
public class StorageController {
    private final SupabaseStorageService storageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<StorageUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new StorageUploadResponse(storageService.upload(file, authentication.getName())));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(
            @RequestParam String publicUrl,
            Authentication authentication) {
        storageService.delete(publicUrl, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
