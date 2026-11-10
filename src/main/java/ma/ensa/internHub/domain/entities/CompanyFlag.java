package ma.ensa.internHub.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CompanyFlag extends AbstractFlag {

    @ManyToOne
    @JoinColumn(name = "flagged_company_id")
    @NotNull(message = "flagged company cannot be null")
    private Company flaggedCompany;

    @ManyToOne
    @JoinColumn(name = "flagged_by_student_id")
    private Student flaggedByStudent;

    @ManyToOne
    @JoinColumn(name = "flagged_by_company_id")
    private Company flaggedByCompany;
}
