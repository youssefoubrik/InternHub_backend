package ma.ensa.internHub.services;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Optional;
import java.util.UUID;
import java.lang.reflect.Proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import ma.ensa.internHub.domain.entities.Student;
import ma.ensa.internHub.exception.BadRequestException;
import ma.ensa.internHub.repositories.UserRepository;

class SupabaseStorageServiceTest {
    private static final String BASE_URL = "https://project.supabase.co";
    private static final String KEY = "test-secret-key";
    private static final String EMAIL = "student@example.com";

    private UserRepository userRepository;
    private MockRestServiceServer server;
    private SupabaseStorageService storageService;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        ownerId = UUID.randomUUID();
        Student owner = Student.builder()
                .id(ownerId)
                .email(EMAIL)
                .password("test-password")
                .firstName("Test")
                .lastName("Student")
                .build();
        userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class<?>[] { UserRepository.class },
                (proxy, method, args) -> {
                    if (method.getName().equals("findByEmail")) return Optional.of(owner);
                    throw new UnsupportedOperationException(method.getName());
                });
        storageService = new SupabaseStorageService(builder, userRepository, BASE_URL, KEY);
    }

    @Test
    void uploadUsesSecretKeyAndReturnsPublicUrlInAuthenticatedUserFolder() {
        byte[] bytes = new byte[] { 1, 2, 3 };
        server.expect(requestTo(org.hamcrest.Matchers.containsString(
                        "/storage/v1/object/images/users/" + ownerId + "/")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("apikey", KEY))
                .andExpect(content().bytes(bytes))
                .andRespond(withSuccess());

        String publicUrl = storageService.upload(
                new MockMultipartFile("file", "profile.png", MediaType.IMAGE_PNG_VALUE, bytes), EMAIL);

        org.junit.jupiter.api.Assertions.assertTrue(publicUrl.startsWith(
                BASE_URL + "/storage/v1/object/public/images/users/" + ownerId + "/"));
        server.verify();
    }

    @Test
    void deleteRemovesOnlyAFileInsideAuthenticatedUsersFolder() {
        String publicUrl = BASE_URL + "/storage/v1/object/public/images/users/" + ownerId + "/photo.png";
        server.expect(requestTo(BASE_URL + "/storage/v1/object/images/users/" + ownerId + "/photo.png"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("apikey", KEY))
                .andRespond(withSuccess());

        storageService.delete(publicUrl, EMAIL);

        server.verify();
    }

    @Test
    void deleteDoesNotCallSupabaseForAnotherUsersObject() {
        String otherUserUrl = BASE_URL + "/storage/v1/object/public/images/users/"
                + UUID.randomUUID() + "/photo.png";

        storageService.delete(otherUserUrl, EMAIL);

        server.verify();
    }

    @Test
    void uploadRejectsUnsupportedContentTypes() {
        assertThrows(BadRequestException.class, () -> storageService.upload(
                new MockMultipartFile("file", "payload.txt", MediaType.TEXT_PLAIN_VALUE, "text".getBytes()), EMAIL));
        server.verify();
    }
}
