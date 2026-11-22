package ma.ensa.internHub.domain.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ensa.internHub.domain.enums.ReportStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyFlagResponse {
    private UUID id;
    private String reason;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private ReportStatus reportStatus;
    private String companyId;
    private String studentId;
    private String companyName;
    private String studentFirstName;
    private String studentLastName;
    private String companyEmail;
    private String studentEmail;
    private String reporterCompanyId;
    private String reporterCompanyName;
    private List<String> screenshots;
}
