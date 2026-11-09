package ma.ensa.internHub.config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import ma.ensa.internHub.domain.entities.Admin;
import ma.ensa.internHub.domain.entities.Application;
import ma.ensa.internHub.domain.entities.Certificat;
import ma.ensa.internHub.domain.entities.Company;
import ma.ensa.internHub.domain.entities.CompanyFlag;
import ma.ensa.internHub.domain.entities.Experience;
import ma.ensa.internHub.domain.entities.Formation;
import ma.ensa.internHub.domain.entities.Internship;
import ma.ensa.internHub.domain.entities.InternshipType;
import ma.ensa.internHub.domain.entities.Links;
import ma.ensa.internHub.domain.entities.Location;
import ma.ensa.internHub.domain.entities.MapLocation;
import ma.ensa.internHub.domain.entities.Project;
import ma.ensa.internHub.domain.entities.SalaryType;
import ma.ensa.internHub.domain.entities.Student;
import ma.ensa.internHub.domain.entities.StudentFlag;
import ma.ensa.internHub.domain.entities.WorkMode;
import ma.ensa.internHub.domain.enums.ApplicationStatus;
import ma.ensa.internHub.domain.enums.ReportStatus;
import ma.ensa.internHub.repositories.AdminRepository;
import ma.ensa.internHub.repositories.ApplicationRepository;
import ma.ensa.internHub.repositories.CertificatRepository;
import ma.ensa.internHub.repositories.CompanyFlagRepository;
import ma.ensa.internHub.repositories.CompanyRepository;
import ma.ensa.internHub.repositories.ExperienceRepository;
import ma.ensa.internHub.repositories.FormationRepository;
import ma.ensa.internHub.repositories.InternshipRepository;
import ma.ensa.internHub.repositories.ProjectRepository;
import ma.ensa.internHub.repositories.StudentFlagRepository;
import ma.ensa.internHub.repositories.StudentRepository;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevelopmentDataSeeder implements CommandLineRunner {
    private static final String ADMIN_EMAIL = "elbanouri.achraf0@gmail.com";
    private static final String ADMIN_PASSWORD = "elbanouri.achraf0";

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;
    private final CompanyRepository companyRepository;
    private final InternshipRepository internshipRepository;
    private final ApplicationRepository applicationRepository;
    private final CompanyFlagRepository companyFlagRepository;
    private final StudentFlagRepository studentFlagRepository;
    private final ExperienceRepository experienceRepository;
    private final FormationRepository formationRepository;
    private final ProjectRepository projectRepository;
    private final CertificatRepository certificatRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${supabase.url:}")
    private String supabaseUrl;

    @Override
    public void run(String... args) {
        seedAdmin();

        Company atlas = company(
                "atlas.company@internhub.local", "Atlas Digital",
                "Atlas Digital builds reliable web products for businesses and is growing its engineering team.",
                "111111111111111", 33.5731, -7.5898);
        Company northstar = company(
                "northstar.company@internhub.local", "Northstar Labs",
                "Northstar Labs helps organizations make better use of data through thoughtful software and analytics.",
                "222222222222222", 34.0209, -6.8416);
        Company shady = company(
                "shadycorp.company@internhub.local", "Shady Corp",
                "A company with questionable practices and poor communication with interns.",
                "333333333333333", 31.6295, -7.9891);
        Company blockedCo = company(
                "blockedco.company@internhub.local", "Blocked Co",
                "This company has been blocked by the admin due to repeated policy violations.",
                "444444444444444", 34.0209, -6.8416);
        blockedCo.setBlocked(true);
        blockedCo.setBlockedAt(LocalDateTime.now());
        companyRepository.save(blockedCo);

        List<Company> featuredCompanies = seedFeaturedCompanies();

        Student sara = student("sara.student@internhub.local", "Sara", "Amrani", "0612345678");
        Student youssef = student("youssef.student@internhub.local", "Youssef", "Idrissi", "0676543210");
        Student leila = student("leila.student@internhub.local", "Leila", "Bennani", "0622334455");
        Student omar = student("omar.student@internhub.local", "Omar", "Tazi", "0633445566");
        Student suspicious = student("suspicious.student@internhub.local", "Suspicious", "Student", "0644556677");
        suspicious.setBlocked(true);
        suspicious.setBlockedAt(LocalDateTime.now());
        studentRepository.save(suspicious);

        List<Student> demoStudents = new ArrayList<>(List.of(sara, youssef, leila, omar));
        demoStudents.addAll(seedAdditionalStudents());
        seedStudentProfiles(demoStudents);
        seedStudentProfiles(List.of(suspicious));

        List<Internship> allInternships = new ArrayList<>();
        allInternships.add(internship(atlas, "Frontend Development Intern",
                "Join our product team to build accessible interfaces with React and TypeScript. You will collaborate with designers and engineers, contribute to production features, and learn how a modern product team ships reliable software.",
                "Casablanca", WorkMode.HYBRID, 4, 5000, true,
                List.of("React", "TypeScript", "CSS"), List.of(InternshipType.PFE, InternshipType.INITIATION)));
        allInternships.add(internship(atlas, "Backend Engineering Intern",
                "Work with our backend team on REST APIs, data models, and service reliability. You will contribute Java code, review changes, write automated tests, and learn practical approaches to secure and maintainable services.",
                "Casablanca", WorkMode.ON_SITE, 6, 6000, true,
                List.of("Java", "Spring Boot", "PostgreSQL"), List.of(InternshipType.PFE)));
        allInternships.add(internship(northstar, "Data Analyst Intern",
                "Help transform business data into clear insights. The internship includes preparing datasets, writing SQL queries, building dashboards, and presenting findings that help teams make informed decisions.",
                "Rabat", WorkMode.REMOTE, 3, 0, false,
                List.of("SQL", "Python", "Power BI"), List.of(InternshipType.INITIATION, InternshipType.PFA)));
        allInternships.add(internship(shady, "Data Entry Intern",
                "Support the operations team by organizing digital records, maintaining spreadsheets, and checking data quality. The role includes clear weekly tasks and regular supervision from the operations lead.",
                "Marrakech", WorkMode.REMOTE, 2, 1500, true,
                List.of("Excel", "Data Entry"), List.of(InternshipType.INITIATION)));
        allInternships.add(internship(blockedCo, "Marketing Intern",
                "Assist the marketing team with social media campaigns, content planning, and performance reporting. You will help prepare promotional materials, monitor engagement, and summarize campaign results.",
                "Rabat", WorkMode.ON_SITE, 3, 3000, true,
                List.of("Marketing", "Social Media"), List.of(InternshipType.PFE)));

        for (Company company : featuredCompanies) {
            allInternships.addAll(seedCompanyInternships(company));
        }

        seedApplications(demoStudents, allInternships);
        application(allInternships.get(3), suspicious, ApplicationStatus.PENDING, "suspicious-cv.pdf");
        application(allInternships.get(4), omar, ApplicationStatus.PENDING, "omar-tazi-marketing-cv.pdf");

        companyFlag(shady, sara, "Unpaid wages after internship completion",
                "The company failed to pay the agreed stipend after the internship ended.", ReportStatus.UNRESOLVED);
        companyFlag(shady, youssef, "Poor working conditions",
                "Interns were asked to work overtime without compensation and without proper supervision.", ReportStatus.WARNED);
        companyFlag(blockedCo, leila, "Misleading job description",
                "The advertised internship role did not match the actual work assigned.", ReportStatus.RESOLVED);
        studentFlag(atlas, suspicious, "Submitted falsified documents",
                "The student provided a forged CV and certificate during the application process.", ReportStatus.UNRESOLVED);
        studentFlag(northstar, suspicious, "Unprofessional behavior",
                "The student was reported for unprofessional conduct during the internship interview.", ReportStatus.WARNED);
    }

    private void seedAdmin() {
        if (adminRepository.findByEmailIgnoreCase(ADMIN_EMAIL).isPresent()) return;
        adminRepository.save(Admin.builder()
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .firstName("Achraf")
                .lastName("Elbanouri")
                .build());
    }

    private Student student(String email, String firstName, String lastName, String tel) {
        return studentRepository.findByEmail(email).orElseGet(() -> studentRepository.save(Student.builder()
                .email(email)
                .password(passwordEncoder.encode("StudentDemo123!"))
                .firstName(firstName)
                .lastName(lastName)
                .school("Ecole Nationale des Sciences Appliquées")
                .profileTitle("Engineering Student")
                .tel(tel)
                .location(Location.builder().country("Morocco").city("Casablanca").address("Campus").build())
                .links(Links.builder().build())
                .profileDescription("Motivated engineering student looking for an opportunity to learn and contribute to real projects.")
                .build()));
    }

    private List<Student> seedAdditionalStudents() {
        return List.of(
                student("hind.elmansouri@internhub.local", "Hind", "El Mansouri", "0661002001"),
                student("adam.belkhayat@internhub.local", "Adam", "Belkhayat", "0661002002"),
                student("salma.alaoui@internhub.local", "Salma", "Alaoui", "0661002003"),
                student("mehdi.chraibi@internhub.local", "Mehdi", "Chraibi", "0661002004"),
                student("nour.elidrissi@internhub.local", "Nour", "El Idrissi", "0661002005"));
    }

    private void seedStudentProfiles(List<Student> students) {
        String[] specialties = { "Full-Stack Software Engineering", "Data Science and Analytics", "Cybersecurity Engineering",
                "Cloud and DevOps Engineering", "Product Design and Frontend Development", "Software Engineering",
                "Artificial Intelligence", "Information Systems", "Computer Networks" };
        String[] schools = { "Ecole Nationale des Sciences Appliquées de Casablanca", "Ecole Mohammadia d'Ingénieurs",
                "Ecole Nationale des Sciences Appliquées de Rabat", "Université Hassan II de Casablanca",
                "Ecole Nationale Supérieure d'Informatique et d'Analyse des Systèmes" };
        String[] cities = { "Casablanca", "Rabat", "Marrakech", "Tangier", "Agadir", "Fes", "Kenitra", "Oujda", "Meknes" };

        for (int i = 0; i < students.size(); i++) {
            Student student = students.get(i);
            String specialty = specialties[i % specialties.length];
            String city = cities[i % cities.length];
            String githubHandle = (student.getFirstName() + student.getLastName()).toLowerCase().replaceAll("[^a-z0-9]", "");
            student.setSchool(schools[i % schools.length]);
            student.setProfileTitle("Final-year " + specialty + " student");
            student.setLocation(Location.builder().country("Morocco").city(city).address("Engineering Campus").build());
            student.setLinks(Links.builder()
                    .github("https://github.com/" + githubHandle)
                    .linkedin("https://www.linkedin.com/in/" + githubHandle)
                    .website("https://portfolio.example.com/" + githubHandle)
                    .cv("https://example.com/demo-cvs/" + githubHandle + "-cv.pdf")
                    .build());
            student.setProfileDescription("Final-year engineering student focused on " + specialty.toLowerCase()
                    + ". Experienced in collaborative projects, clear technical communication, and delivering thoughtful solutions. "
                    + "Seeking an internship where I can contribute to a professional team and continue developing practical skills.");
            studentRepository.save(student);

            if (experienceRepository.findByStudentId(student.getId()).isEmpty()) {
                experienceRepository.save(Experience.builder().student(student)
                        .poste(i % 2 == 0 ? "Software Engineering Intern" : "Junior Developer Intern")
                        .company(i % 2 == 0 ? "University Innovation Lab" : "Digital Solutions Studio")
                        .startDate("Jun-2024").endDate("Aug-2024")
                        .description("Contributed to a supervised software project by implementing features, reviewing requirements, and collaborating with a small cross-functional team.")
                        .build());
                experienceRepository.save(Experience.builder().student(student)
                        .poste("Technology Project Contributor")
                        .company("" + student.getSchool())
                        .startDate("Oct-2023").endDate("May-2024")
                        .description("Worked with classmates to plan, build, and present an applied engineering project using version control and iterative feedback.")
                        .build());
            }
            if (formationRepository.findByStudentId(student.getId()).isEmpty()) {
                formationRepository.save(Formation.builder().student(student).company(student.getSchool())
                        .domain(specialty).diploma("Engineering Degree").startDate("Sep-2022").endDate("Jun-2026").build());
                formationRepository.save(Formation.builder().student(student).company("Preparatory Classes for Engineering Schools")
                        .domain("Mathematics, Physics and Computer Science").diploma("Preparatory Cycle")
                        .startDate("Sep-2020").endDate("Jun-2022").build());
            }
            if (projectRepository.findByStudentId(student.getId()).isEmpty()) {
                projectRepository.save(Project.builder().student(student).title("Smart Internship Tracker")
                        .image("https://placehold.co/800x500/png?text=Internship+Tracker")
                        .link("https://github.com/" + githubHandle + "/internship-tracker").build());
                projectRepository.save(Project.builder().student(student).title("Data Insights Dashboard")
                        .image("https://placehold.co/800x500/png?text=Data+Dashboard")
                        .link("https://github.com/" + githubHandle + "/data-dashboard").build());
            }
            if (certificatRepository.findByStudentId(student.getId()).isEmpty()) {
                certificatRepository.save(Certificat.builder().student(student).title("Java Programming Foundations")
                        .thumbnail("https://placehold.co/500x350/png?text=Java+Certificate").date("2024").build());
                certificatRepository.save(Certificat.builder().student(student).title("Cloud Computing Essentials")
                        .thumbnail("https://placehold.co/500x350/png?text=Cloud+Certificate").date("2025").build());
            }
        }
    }

    private List<Company> seedFeaturedCompanies() {
        return List.of(
                corporateCompany("oracle", "Oracle", "Oracle builds integrated cloud applications and infrastructure that help organizations run critical systems, manage data, and develop modern digital services. Teams work across enterprise software, cloud platforms, databases, and applied AI.", "555555555555551", 33.5899, -7.6039, "Oracle.png", "0522001001", "https://www.oracle.com", "https://www.linkedin.com/company/oracle/"),
                corporateCompany("capgemini", "Capgemini", "Capgemini is a global partner for business and technology transformation. Its teams combine engineering, consulting, data, cloud, and design capabilities to help clients build useful, secure, and sustainable digital products.", "555555555555552", 33.5731, -7.5898, "capgemini.png", "0522001002", "https://www.capgemini.com", "https://www.linkedin.com/company/capgemini/"),
                corporateCompany("bcg", "Boston Consulting Group", "Boston Consulting Group works with organizations to solve complex business challenges and deliver lasting change. Its teams apply strategy, technology, analytics, and design to create measurable results across industries.", "555555555555553", 33.5892, -7.6114, "bcg.png", "0522001003", "https://www.bcg.com", "https://www.linkedin.com/company/boston-consulting-group/"),
                corporateCompany("google", "Google", "Google develops products and platforms that organize information and make technology useful to people and businesses. Engineering teams work on large-scale systems, cloud services, AI, security, and products used around the world.", "555555555555554", 33.5731, -7.5898, "google.png", "0522001004", "https://about.google", "https://www.linkedin.com/company/google/"),
                corporateCompany("microsoft", "Microsoft", "Microsoft creates software, cloud services, devices, and AI tools that empower people and organizations. Its teams build secure platforms and products spanning Azure, Microsoft 365, developer tools, and business applications.", "555555555555555", 33.5731, -7.5898, "microsoft.jpeg", "0522001005", "https://www.microsoft.com", "https://www.linkedin.com/company/microsoft/"),
                corporateCompany("meta", "Meta", "Meta builds technologies that help people connect and share. Product and engineering teams work on social platforms, messaging, infrastructure, applied AI, privacy, and immersive technologies for a global community.", "555555555555556", 33.5731, -7.5898, "meta.jpeg", "0522001006", "https://about.meta.com", "https://www.linkedin.com/company/meta/"),
                corporateCompany("hps", "HPS (Hightech Payment Systems)", "HPS is a global provider of payment solutions, supporting financial institutions, processors, and businesses with secure digital payment platforms. Its teams work across payment technology, cybersecurity, software engineering, and financial services.", "555555555555557", 33.5731, -7.5898, "hps.jpeg", "0522001007", "https://www.hps-worldwide.com", "https://www.linkedin.com/company/hightech-payment-systems/"),
                corporateCompany("ibm", "IBM", "IBM helps organizations apply hybrid cloud, AI, and enterprise technology to complex business and operational challenges. Its teams combine research, software, consulting, and infrastructure expertise to build trusted solutions.", "555555555555558", 33.5731, -7.5898, "ibm.png", "0522001008", "https://www.ibm.com", "https://www.linkedin.com/company/ibm/"),
                corporateCompany("orange", "Orange Maroc", "Orange Maroc connects people and businesses through mobile, broadband, and digital services. Its teams build and operate reliable networks while developing customer experiences and digital products for individuals and organizations.", "555555555555559", 33.5731, -7.5898, "orange.png", "0522001009", "https://www.orange.ma", "https://www.linkedin.com/company/orange/"),
                corporateCompany("inwi", "inwi", "inwi is a Moroccan telecommunications and digital services operator serving consumers, entrepreneurs, and enterprises. Its teams work on connectivity, cloud, cybersecurity, data, and digital experiences that support Morocco's digital transformation.", "555555555555560", 33.5731, -7.5898, "inwi.png", "0522001010", "https://inwi.ma", "https://www.linkedin.com/company/inwi/"),
                corporateCompany("sqli", "SQLI Maroc", "SQLI is a digital services company that helps brands design, build, and improve customer experiences and digital commerce platforms. Multidisciplinary teams deliver consulting, design, engineering, and data capabilities for international clients.", "555555555555561", 33.5731, -7.5898, "sqli.png", "0522001011", "https://www.sqli.com", "https://www.linkedin.com/company/sqli/"));
    }

    private Company corporateCompany(String slug, String name, String description, String ice,
            double latitude, double longitude, String logo, String tel, String website, String linkedin) {
        Company company = companyRepository.findByEmail(slug + ".company@internhub.local")
                .orElseGet(() -> Company.builder()
                        .email(slug + ".company@internhub.local")
                        .password(passwordEncoder.encode("CompanyDemo123!"))
                        .name(name).build());
        company.setName(name);
        company.setDescription(description);
        company.setIce(ice);
        company.setTel(tel);
        company.setLocation(MapLocation.builder().latitude(latitude).longitude(longitude).build());
        company.setLinks(Links.builder().website(website).linkedin(linkedin).build());
        company.setProfilePicture(publicCompanyLogoUrl(logo));
        company.setBlocked(false);
        return companyRepository.save(company);
    }

    private String publicCompanyLogoUrl(String filename) {
        if (filename == null || filename.isBlank() || supabaseUrl == null || supabaseUrl.isBlank()) return null;
        return supabaseUrl.replaceAll("/$", "") + "/storage/v1/object/public/images/companies/" + filename;
    }

    private List<Internship> seedCompanyInternships(Company company) {
        String companyName = company.getName();
        List<Internship> seeded = new ArrayList<>();
        seeded.add(internship(company, "Software Engineering Intern — " + companyName,
                "Collaborate with experienced engineers to design, implement, and test production-quality software. You will contribute to a scoped team project, participate in code reviews, document technical decisions, and learn the engineering practices used to deliver secure and maintainable products.",
                "Casablanca", WorkMode.HYBRID, 6, 6500, true,
                List.of("Java", "REST APIs", "Git", "SQL"), List.of(InternshipType.PFE, InternshipType.PFA)));
        seeded.add(internship(company, "Data and Cloud Intern — " + companyName,
                "Join a collaborative data and cloud team to support reliable digital services. You will prepare and analyze datasets, build or improve cloud workflows, document your work, and present practical recommendations to mentors and project stakeholders.",
                "Rabat", WorkMode.HYBRID, 5, 6000, true,
                List.of("Python", "SQL", "Cloud", "Analytics"), List.of(InternshipType.PFE, InternshipType.INITIATION)));
        return seeded;
    }

    private Company company(String email, String name, String description, String ice, double latitude, double longitude) {
        return companyRepository.findByEmail(email).orElseGet(() -> companyRepository.save(Company.builder()
                .email(email).password(passwordEncoder.encode("CompanyDemo123!"))
                .name(name).description(description).ice(ice).tel("0522123456")
                .location(MapLocation.builder().latitude(latitude).longitude(longitude).build())
                .links(Links.builder().build()).blocked(false).build()));
    }

    private Internship internship(Company company, String title, String description, String city,
            WorkMode workMode, int duration, double salary, boolean paid, List<String> skills, List<InternshipType> tags) {
        return internshipRepository.findByCompanyId(company.getId()).stream()
                .filter(existing -> title.equals(existing.getTitle()))
                .findFirst()
                .orElseGet(() -> internshipRepository.save(Internship.builder()
                        .company(company).title(title).description(description).city(city).workMode(workMode)
                        .duration(duration).salary(salary).salaryType(SalaryType.MONTH).paid(paid)
                        .negotiable(false).motivationLetterRequired(false).skills(skills).tags(tags).build()));
    }

    private void seedApplications(List<Student> students, List<Internship> internships) {
        ApplicationStatus[] statuses = { ApplicationStatus.PENDING, ApplicationStatus.ACCEPTED,
                ApplicationStatus.REJECTED, ApplicationStatus.PENDING };
        for (int studentIndex = 0; studentIndex < students.size(); studentIndex++) {
            Student student = students.get(studentIndex);
            for (int applicationIndex = 0; applicationIndex < 5; applicationIndex++) {
                Internship internship = internships.get((studentIndex * 3 + applicationIndex * 4) % internships.size());
                application(internship, student, statuses[(studentIndex + applicationIndex) % statuses.length],
                        student.getFirstName().toLowerCase() + "-" + student.getLastName().toLowerCase() + "-cv.pdf");
            }
        }
    }

    private void application(Internship internship, Student student, ApplicationStatus status, String cvName) {
        if (applicationRepository.existsByInternshipIdAndStudentId(internship.getId(), student.getId())) return;
        applicationRepository.save(Application.builder().internship(internship).student(student).status(status)
                .cv("https://example.com/demo-cvs/" + cvName).motivationLetter("").build());
    }

    private void companyFlag(Company company, Student student, String reason, String description, ReportStatus status) {
        boolean alreadySeeded = companyFlagRepository.findAll().stream().anyMatch(flag ->
                flag.getFlaggedCompany().getId().equals(company.getId())
                        && flag.getFlaggedByStudent().getId().equals(student.getId())
                        && flag.getReason().equals(reason));
        if (alreadySeeded) return;
        companyFlagRepository.save(CompanyFlag.builder().flaggedCompany(company).flaggedByStudent(student)
                .reason(reason).description(description).reportStatus(status).screenshots(List.of()).build());
    }

    private void studentFlag(Company company, Student student, String reason, String description, ReportStatus status) {
        boolean alreadySeeded = studentFlagRepository.findAll().stream().anyMatch(flag ->
                flag.getFlaggedStudent().getId().equals(student.getId())
                        && flag.getFlaggedByCompany().getId().equals(company.getId())
                        && flag.getReason().equals(reason));
        if (alreadySeeded) return;
        studentFlagRepository.save(StudentFlag.builder().flaggedByCompany(company).flaggedStudent(student)
                .reason(reason).description(description).reportStatus(status).screenshots(List.of()).build());
    }
}
