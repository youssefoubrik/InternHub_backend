package ma.ensa.internHub.services.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ma.ensa.internHub.domain.dto.request.EmailWithAttachmentsRequest;
import ma.ensa.internHub.domain.dto.request.NotificationRequest;
import ma.ensa.internHub.domain.dto.request.CreateFlagRequest;
import ma.ensa.internHub.domain.dto.response.CompanyFlagResponse;
import ma.ensa.internHub.domain.dto.response.FlaggedCompanyOverview;
import ma.ensa.internHub.domain.entities.Company;
import ma.ensa.internHub.domain.entities.CompanyFlag;
import ma.ensa.internHub.domain.entities.Student;
import ma.ensa.internHub.domain.enums.ReportStatus;
import ma.ensa.internHub.exception.BadRequestException;
import ma.ensa.internHub.mappers.CompanyFlagMapper;
import ma.ensa.internHub.repositories.CompanyFlagRepository;
import ma.ensa.internHub.repositories.CompanyRepository;
import ma.ensa.internHub.repositories.StudentRepository;
import ma.ensa.internHub.services.CompanyFlagService;
import ma.ensa.internHub.services.EmailNotificationService;
import ma.ensa.internHub.utils.AuthUtils;

@Service
@RequiredArgsConstructor
public class CompanyFlagServiceImpl implements CompanyFlagService {
    private final CompanyFlagRepository companyFlagRepository;
    private final CompanyFlagMapper companyFlagMapper;
    private final EmailNotificationService emailNotificationService;
    private final CompanyRepository companyRepository;
    private final StudentRepository studentRepository;
    private final AuthUtils authUtils;

    @Override
    public long countUnresolvedCompanyFlags() {
        return companyFlagRepository.countByReportStatus(ReportStatus.UNRESOLVED);
    }

    @Override
    public List<FlaggedCompanyOverview> getFlaggedCompaniesOverview() {
        return companyFlagRepository.findFlaggedCompaniesOverviewByStatus(ReportStatus.UNRESOLVED);
    }

    @Override
    public long getUnresolvedCompanyFlagsCountById(UUID id) {
        return companyFlagRepository.countByFlaggedCompany_IdAndReportStatus(id, ReportStatus.UNRESOLVED);
    }

    @Override
    public long getResolvedFlagsCountByCompanyId(UUID id) {
        return companyFlagRepository.countByFlaggedCompany_IdAndReportStatus(id, ReportStatus.RESOLVED);
    }

    @Override
    public long getCompanyWarningsCount(UUID id) {
        return companyFlagRepository.countByFlaggedCompany_IdAndReportStatus(id, ReportStatus.WARNED);
    }

    @Override
    public List<CompanyFlagResponse> getCompanyFlagsHistory(UUID id) {
        return companyFlagRepository.findByFlaggedCompanyId(id).stream()
                .map(companyFlagMapper::toResponse)
                .toList();
    }

    @Override
    public void ignoreCompanyFlag(UUID id) {
        companyFlagRepository.findById(id)
                .map(companyFlag -> {
                    if (!(companyFlag.getReportStatus() == ReportStatus.UNRESOLVED)) {
                        throw new BadRequestException("Flag already resolved or company warned");
                    }
                    companyFlag.setReportStatus(ReportStatus.RESOLVED);
                    return companyFlagRepository.save(companyFlag);
                })
                .orElseThrow(() -> new EntityNotFoundException("Company flag not found with id: " + id));
    }

    @Override
    public void warnCompany(UUID id, NotificationRequest request) {
        var companyFlag = companyFlagRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Company flag not found with id: " + id));

        if (companyFlag.getReportStatus() != ReportStatus.UNRESOLVED) {
            throw new BadRequestException("Flag already resolved or company warned");
        }

        emailNotificationService.sendDynamicEmailWithMultipartAttachments(
                EmailWithAttachmentsRequest.builder().to(companyFlag.getFlaggedCompany().getEmail())
                        .subject(request.getSubject())
                        .recepientName(companyFlag.getFlaggedCompany().getName())
                        .htmlBody(request.getMessage())
                        .attachments(request.getAttachments()).build());

        companyFlag.setReportStatus(ReportStatus.WARNED);
        companyFlagRepository.save(companyFlag);
    }

    @Override
    public void createCompanyFlag(CreateFlagRequest request) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isCompany = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_COMPANY"));
        Student flaggingStudent = isCompany ? null : authUtils.getCurrentStudent();
        Company flaggingCompany = isCompany ? authUtils.getCurrentCompany() : null;
        Company flaggedCompany = companyRepository.findById(request.getTargetId())
                .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + request.getTargetId()));

        if (flaggingCompany != null && flaggingCompany.getId().equals(flaggedCompany.getId())) {
            throw new BadRequestException("A company cannot flag itself");
        }

        CompanyFlag companyFlag = CompanyFlag.builder()
                .flaggedCompany(flaggedCompany)
                .flaggedByStudent(flaggingStudent)
                .flaggedByCompany(flaggingCompany)
                .reason(request.getReason())
                .description(request.getDescription())
                .screenshots(request.getScreenshots())
                .reportStatus(ReportStatus.UNRESOLVED)
                .build();

        companyFlagRepository.save(companyFlag);
    }
}
