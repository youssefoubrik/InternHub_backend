package ma.ensa.internHub.domain.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Internship extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Description is required")
    @Size(min = 120, message = "Description should be at least 120 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @Positive(message = "Duration must be positive")
    private int duration;

    private double salary;

    @NotNull(message = "Salary type is required")
    @Enumerated(EnumType.STRING)
    private SalaryType salaryType;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Work mode is required")
    @Enumerated(EnumType.STRING)
    private WorkMode workMode;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    private List<InternshipType> tags;

    @NotNull(message = "At least one skill must be specified")
    @Size(min = 1, message = "You must provide at least one skill")
    @ElementCollection
    private List<@NotBlank(message = "Skill cannot be blank") String> skills;

    @NotNull(message = "negotiable status is required")
    private boolean negotiable;

    private boolean paid;

    private boolean closed;

    @NotNull(message = "City is required")
    private String city;

    @NotNull(message = "motivation letter is required")
    private boolean motivationLetterRequired;

    @OneToMany(mappedBy = "internship", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Application> applications = new ArrayList<>();

    @NotNull(message = "Company is required")
    @ManyToOne
    private Company company;
}
