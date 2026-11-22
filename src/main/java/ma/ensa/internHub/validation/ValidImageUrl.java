package ma.ensa.internHub.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidImageUrlValidator.class)
@Documented
public @interface ValidImageUrl {
    String message() default "Invalid image URL format. Must end with .jpg, .jpeg, .png";

    String[] allowedExtensions() default { ".jpg", ".jpeg", ".png", ".webp", ".gif" };

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long maxFileSize() default 5_242_880L;
}
