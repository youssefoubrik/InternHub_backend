package ma.ensa.internHub.services;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import ma.ensa.internHub.domain.entities.User;
import ma.ensa.internHub.exception.BadRequestException;
import ma.ensa.internHub.exception.ResourceNotFoundException;
import ma.ensa.internHub.repositories.UserRepository;

@Service
public class SupabaseStorageService {
    private static final String BUCKET = "images";
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif",
            "application/pdf", "pdf");

    private final RestClient restClient;
    private final UserRepository userRepository;
    private final String supabaseUrl;
    private final String supabaseKey;

    public SupabaseStorageService(
            RestClient.Builder restClientBuilder,
            UserRepository userRepository,
            @Value("${supabase.url:}") String supabaseUrl,
            @Value("${supabase.key:}") String supabaseKey) {
        this.restClient = restClientBuilder.build();
        this.userRepository = userRepository;
        this.supabaseUrl = supabaseUrl == null ? "" : supabaseUrl.replaceAll("/+$", "");
        this.supabaseKey = supabaseKey == null ? "" : supabaseKey;
    }

    public String upload(MultipartFile file, String email) {
        requireConfiguration();
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File must be 10 MB or smaller");
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String extension = ALLOWED_TYPES.get(contentType);
        if (extension == null) {
            throw new BadRequestException("Only JPEG, PNG, WebP, GIF, and PDF files are supported");
        }

        User owner = getOwner(email);
        String objectPath = "users/" + owner.getId() + "/" + UUID.randomUUID() + "." + extension;
        restClient.post()
                .uri(objectUri(objectPath))
                .header("apikey", supabaseKey)
                .contentType(MediaType.parseMediaType(contentType))
                .header("x-upsert", "false")
                .body(file.getResource())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new IllegalStateException("Supabase upload failed with status " + response.getStatusCode());
                })
                .toBodilessEntity();

        return publicUrl(objectPath);
    }

    /**
     * Deletes only objects in the authenticated user's server-assigned folder.
     * Older client-created objects are deliberately left untouched.
     */
    public void delete(String publicUrl, String email) {
        requireConfiguration();
        User owner = getOwner(email);
        String objectPath = getOwnedObjectPath(publicUrl, owner.getId().toString());
        if (objectPath == null) {
            return;
        }

        restClient.delete()
                .uri(objectUri(objectPath))
                .header("apikey", supabaseKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new IllegalStateException("Supabase delete failed with status " + response.getStatusCode());
                })
                .toBodilessEntity();
    }

    private User getOwner(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private String getOwnedObjectPath(String publicUrl, String userId) {
        if (!StringUtils.hasText(publicUrl)) {
            return null;
        }
        try {
            URI uri = URI.create(publicUrl);
            URI base = URI.create(supabaseUrl);
            if (uri.getHost() == null || base.getHost() == null
                    || !uri.getScheme().equalsIgnoreCase(base.getScheme())
                    || !uri.getHost().equalsIgnoreCase(base.getHost())
                    || uri.getPort() != base.getPort()) {
                return null;
            }
            String marker = "/storage/v1/object/public/" + BUCKET + "/";
            if (!uri.getPath().startsWith(marker)) {
                return null;
            }
            String encodedPath = uri.getPath().substring(marker.length());
            String path = java.net.URLDecoder.decode(encodedPath, java.nio.charset.StandardCharsets.UTF_8);
            return path.startsWith("users/" + userId + "/") && !path.contains("..") ? path : null;
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private URI objectUri(String objectPath) {
        String encodedPath = java.util.Arrays.stream(objectPath.split("/"))
                .map(segment -> java.net.URLEncoder.encode(segment, java.nio.charset.StandardCharsets.UTF_8)
                        .replace("+", "%20"))
                .collect(java.util.stream.Collectors.joining("/"));
        return URI.create(supabaseUrl + "/storage/v1/object/" + BUCKET + "/" + encodedPath);
    }

    private String publicUrl(String objectPath) {
        String encodedPath = java.util.Arrays.stream(objectPath.split("/"))
                .map(segment -> java.net.URLEncoder.encode(segment, java.nio.charset.StandardCharsets.UTF_8)
                        .replace("+", "%20"))
                .collect(java.util.stream.Collectors.joining("/"));
        return supabaseUrl + "/storage/v1/object/public/" + BUCKET + "/" + encodedPath;
    }

    private void requireConfiguration() {
        if (!StringUtils.hasText(supabaseUrl) || !StringUtils.hasText(supabaseKey)) {
            throw new IllegalStateException("Supabase storage is not configured; set SUPABASE_URL and SUPABASE_KEY");
        }
    }
}
