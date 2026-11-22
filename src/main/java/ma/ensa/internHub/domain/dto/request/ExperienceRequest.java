package ma.ensa.internHub.domain.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ensa.internHub.validation.ValidDate;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceRequest {

    @NotBlank(message = "Poste is required")
    private String poste;

    @ValidDate(message = "Start date must be in MMM-yyyy format ")
    private String startDate;

    @ValidDate(message = "End date must be in MMM-yyyy format or 'PRESENT'")
    private String endDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "Company name is required")
    private String company; // updated

    @NotNull(message = "Student ID is required")
    private UUID studentId;

}
