package ma.ensa.internHub.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ensa.internHub.domain.entities.Links;
import ma.ensa.internHub.domain.entities.MapLocation;
import ma.ensa.internHub.validation.Phone;
import ma.ensa.internHub.validation.ValidImageUrl;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUpdateRequest {
    @NotBlank(message = "Company name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Phone
    private String tel;

    @Size(min = 10, max = 500, message = "Description must be between 10 and 500 characters")
    private String description;

    @Pattern(regexp = "^(?:\\s*|\\d{15})$", message = "ICE must be exactly 15 digits")
    @NotBlank(message = "ICE is required")
    private String ice;

    private MapLocation location;

    @ValidImageUrl
    private String profilePicture;

    private Links links;
}
