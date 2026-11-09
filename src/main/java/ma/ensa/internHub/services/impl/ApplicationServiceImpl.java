package ma.ensa.internHub.services.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import ma.ensa.internHub.domain.dto.request.ApplicationRequest;
import ma.ensa.internHub.domain.dto.request.EmailWithAttachmentsRequest;
import ma.ensa.internHub.domain.dto.request.NotificationRequest;
import ma.ensa.internHub.domain.dto.response.ApplicationResponse;
import ma.ensa.internHub.domain.entities.Application;
import ma.ensa.internHub.domain.entities.Internship;
import ma.ensa.internHub.domain.entities.Student;
import ma.ensa.internHub.domain.enums.ApplicationStatus;
import ma.ensa.internHub.mappers.ApplicationMapper;
import ma.ensa.internHub.mappers.InternshipMapper;
import ma.ensa.internHub.mappers.StudentMapper;
import ma.ensa.internHub.repositories.ApplicationRepository;
import ma.ensa.internHub.repositories.InternshipRepository;
import ma.ensa.internHub.repositories.StudentRepository;
import ma.ensa.internHub.services.ApplicationService;
import ma.ensa.internHub.services.EmailNotificationService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import ma.ensa.internHub.exception.BadRequestException;
import ma.ensa.internHub.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final InternshipRepository internshipRepository;
    private final StudentRepository studentRepository;
    private final ApplicationMapper applicationMapper;
    private final StudentMapper studentMapper;
    private final InternshipMapper internshipMapper;
    private final EmailNotificationService emailNotificationService;

    @Override
    public List<ApplicationResponse> getApplicationsByCompanyId(UUID companyId) {
        return applicationRepository.findByInternshipCompanyId(companyId).stream()
                .map(application -> {
                    ApplicationResponse response = applicationMapper.toResponse(application);
                    response.setStudentResponse(studentMapper.toResponse(application.getStudent()));
                    response.setInternshipResponse(internshipMapper.toResponse(application.getInternship()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ApplicationResponse> getApplicationsByInternshipId(UUID internshipId) {
        return applicationRepository.findByInternshipId(internshipId).stream()
                .map(application -> {
                    ApplicationResponse response = applicationMapper.toResponse(application);
                    response.setStudentResponse(studentMapper.toResponse(application.getStudent()));
                    response.setInternshipResponse(internshipMapper.toResponse(application.getInternship()));
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Long countApplicationsByCompanyId(UUID companyId) {
        return applicationRepository.countByInternshipCompanyId(companyId); // added here
    }

    @Override
    public Boolean isAppliedToThisInternship(UUID internshipId, UUID studentId) {
        return applicationRepository.existsByInternshipIdAndStudentId(internshipId, studentId); // added here
    }

    @Override
    public Long countApplicationsByInternshipId(UUID internshipId) {
        return applicationRepository.countByInternshipId(internshipId); // added here
    }

    @Override
    public Long countApplicationsByCompanyIdWithStatus(UUID companyId, ApplicationStatus status) {
        return applicationRepository.countByInternshipCompanyIdAndStatus(companyId, status); // added here
    }

    @Override
    public Long countDistinctStudents() {
        return applicationRepository.countDistinctStudentId();
    }

    @Override
    public Long countDistinctStudentsByInternshipStatus(ApplicationStatus status) {
        return applicationRepository.countDistinctStudentIdByInternshipStatus(status);
    }
    
    @Override
    public List<ApplicationResponse> getApplicationsByStudentId(UUID studentId){
        studentRepository.findById(studentId)
            .orElseThrow(() -> new EntityNotFoundException("Student Not Found"));
        return applicationRepository.findByStudentId(studentId).stream()
            .map(application -> {
                ApplicationResponse response = applicationMapper.toResponse(application);
                response.setStudentResponse(studentMapper.toResponse(application.getStudent()));
                response.setInternshipResponse(internshipMapper.toResponse(application.getInternship()));
                return response;
            }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + email));

        Internship internship = internshipRepository.findById(request.getInternshipId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Internship not found with ID: " + request.getInternshipId()));
        
        applicationRepository.findByInternshipIdAndStudentId(request.getInternshipId(), student.getId())
                .ifPresent(app -> {
                    throw new BadRequestException("Student already applied to this internship");
                });

        Application application = applicationMapper.toEntity(request);
        application.setStudent(student);
        application.setInternship(internship);

        Application saved;
        try {
            saved = applicationRepository.saveAndFlush(application);
        } catch (DataIntegrityViolationException exception) {
            throw new BadRequestException("Student already applied to this internship");
        }

        ApplicationResponse response = applicationMapper.toResponse(saved);
        response.setStudentResponse(studentMapper.toResponse(saved.getStudent()));
        response.setInternshipResponse(internshipMapper.toResponse(saved.getInternship()));
        return response;
    }

    @Override
    public void acceptApplication(UUID id, NotificationRequest request) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application not found with id: " + id));

        if (application.getStatus().equals(ApplicationStatus.ACCEPTED)) {
            throw new BadRequestException("Application is already accepted");
        }

        if (application.getStatus().equals(ApplicationStatus.REJECTED)) {
            throw new BadRequestException("Application is already rejected");
        }

        emailNotificationService.sendDynamicEmailWithMultipartAttachments(
                EmailWithAttachmentsRequest.builder()
                        .to(application.getStudent().getEmail())
                        .subject(request.getSubject())
                        .recepientName(application.getStudent().getFirstName())
                        .htmlBody(request.getMessage())
                        .attachments(request.getAttachments())
                        .build());

        application.setStatus(ApplicationStatus.ACCEPTED);
        applicationRepository.save(application);
    }

    @Override
    public void rejectApplication(UUID id, NotificationRequest request) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Application not found with id: " + id));

        if (application.getStatus().equals(ApplicationStatus.ACCEPTED)) {
            throw new BadRequestException("Application is already accepted");
        }

        if (application.getStatus().equals(ApplicationStatus.REJECTED)) {
            throw new BadRequestException("Application is already rejected");
        }

        emailNotificationService.sendDynamicEmailWithMultipartAttachments(
                EmailWithAttachmentsRequest.builder()
                        .to(application.getStudent().getEmail())
                        .subject(request.getSubject())
                        .recepientName(application.getStudent().getFirstName())
                        .htmlBody(request.getMessage())
                        .attachments(request.getAttachments())
                        .build());

        application.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(application);
    }

    @Override
    public void removeApplicationById(UUID id) {
        if (!applicationRepository.existsById(id)) {
            throw new EntityNotFoundException("Application not found with ID: " + id);
        }
        applicationRepository.deleteById(id);
    }
}
