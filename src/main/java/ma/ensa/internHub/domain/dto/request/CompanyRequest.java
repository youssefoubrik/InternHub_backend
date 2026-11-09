package ma.ensa.internHub.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ma.ensa.internHub.domain.entities.Links;
import ma.ensa.internHub.domain.entities.MapLocation;
import ma.ensa.internHub.validation.Phone;
import ma.ensa.internHub.validation.ValidImageUrl;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class CompanyRequest extends UserRequest {
    @NotBlank(message = "Company name is required")
    private String name;

    @Size(min = 10, max = 10000, message = "Description must be between 10 and 10000 characters")
    private String description;

    @Pattern(regexp = "^(?:\\s*|\\d{15})$", message = "ICE must be exactly 15 digits")
    private String ice;

    private MapLocation location;

    @Phone
    private String tel;

    @ValidImageUrl
    private String profilePicture;

    private Links links;
}
