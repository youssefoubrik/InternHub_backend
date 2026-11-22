package ma.ensa.internHub.domain.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.ensa.internHub.domain.enums.ApplicationStatus;


@Entity
@Table(uniqueConstraints = @UniqueConstraint(
        name = "uk_application_student_internship",
        columnNames = { "student_id", "internship_id" }))
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    @NotNull(message = "Application date is required")
    private LocalDateTime applicationDate;

    private String motivationLetter;

    @NotBlank(message = "CV is required")
    private String cv;

    // add this function to manually enter the value of applicationDate
    @PrePersist
    private void onCreate() {
        this.applicationDate = LocalDateTime.now();
    }

    @ManyToOne
    @NotNull(message = "Internship is required")
    private Internship internship;
    @ManyToOne
    @NotNull(message = "Student is required")
    private Student student;
}
