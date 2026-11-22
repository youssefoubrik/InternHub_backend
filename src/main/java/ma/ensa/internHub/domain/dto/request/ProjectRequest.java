package ma.ensa.internHub.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ensa.internHub.validation.ValidImageUrl;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @ValidImageUrl
    private String image;

    @Pattern(regexp = "^(?:\\s*|https?://\\S+)$", message = "Project link must start with http:// or https://")
    private String link;

    @NotNull(message = "Student Id is required" )
    private UUID studentId;

}
