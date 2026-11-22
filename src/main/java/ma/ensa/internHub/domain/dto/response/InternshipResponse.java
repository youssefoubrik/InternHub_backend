package ma.ensa.internHub.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ensa.internHub.domain.entities.InternshipType;
import ma.ensa.internHub.domain.entities.Location;
import ma.ensa.internHub.domain.entities.SalaryType;
import ma.ensa.internHub.domain.entities.WorkMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InternshipResponse {

    private UUID id;

    private CompanyResponse company;

    private String city;

    private String description;

    private LocalDateTime createdAt;

    private int duration;

    private double salary;

    private SalaryType salaryType;

    private boolean motivationLetterRequired;

    private String title;

    private WorkMode workMode;

    private List<InternshipType> tags;

    private List<String> skills;

    private boolean negotiable;

    private boolean paid;

    private boolean closed;

}
