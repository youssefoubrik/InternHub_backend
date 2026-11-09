package ma.ensa.internHub.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;
import java.util.Arrays;

public class ValidImageUrlValidator implements ConstraintValidator<ValidImageUrl, String> {
    private String[] allowedExtensions;
    // private long maxFileSize;

    @Override
    public void initialize(ValidImageUrl constraintAnnotation) {
        this.allowedExtensions = constraintAnnotation.allowedExtensions();
        // this.maxFileSize = constraintAnnotation.maxFileSize();
    }

    @Override
    public boolean isValid(String urlString, ConstraintValidatorContext context) {
        if (urlString == null || urlString.isEmpty()) {
            return true;
        }
        try {
            URI uri = URI.create(urlString);
            String scheme = uri.getScheme();
            if (uri.getHost() == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
                return false;
            }
            String path = uri.getPath().toLowerCase();

            boolean hasValidExtension = Arrays.stream(allowedExtensions)
                    .anyMatch(extension -> path.endsWith(extension.toLowerCase()));
            return hasValidExtension;

        } catch (Exception e) {
            return false;
        }
    }
}
