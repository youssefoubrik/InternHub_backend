package ma.ensa.internHub.services.impl;

import lombok.RequiredArgsConstructor;
import ma.ensa.internHub.domain.dto.request.InternshipRequest;
import ma.ensa.internHub.domain.dto.response.CompanyResponse;
import ma.ensa.internHub.domain.dto.response.InternshipResponse;
import ma.ensa.internHub.domain.entities.Company;
import ma.ensa.internHub.domain.entities.Internship;
import ma.ensa.internHub.domain.entities.InternshipType;
import ma.ensa.internHub.domain.entities.WorkMode;
import ma.ensa.internHub.exception.ResourceNotFoundException;
import ma.ensa.internHub.mappers.CompanyMapper;
import ma.ensa.internHub.mappers.InternshipMapper;
import ma.ensa.internHub.repositories.CompanyRepository;
import ma.ensa.internHub.repositories.InternshipRepository;
import ma.ensa.internHub.services.InternshipService;
import ma.ensa.internHub.specifications.InternshipSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InternshipServiceImpl implements InternshipService {

    private final InternshipRepository internshipRepository;
    private final CompanyRepository companyRepository;
    private final InternshipMapper internshipMapper;
    private final CompanyMapper companyMapper;

    @Override
    public long countInternshipsByWorkMode(WorkMode workMode) {
        return internshipRepository.countByWorkMode(workMode);
    }

    @Override
    public long countAllInternships() {
        return internshipRepository.count();
    }

    @Override
    public long countInternshipsByCompanyId(UUID companyId) { // added
        return internshipRepository.countByCompanyId(companyId);
    }

    @Override
    @Transactional
    public InternshipResponse saveInternship(InternshipRequest request) {
        Authentication authenticatedUser = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedUser.getName();

        Company company = companyRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Company not found with email: " + email));

        Internship internship = internshipMapper.toEntity(request);
        internship.setCompany(company);
        Internship savedInternship = internshipRepository.save(internship);

        CompanyResponse companyResponse = companyMapper.toResponse(company);
        InternshipResponse response = internshipMapper.toResponse(savedInternship);
        response.setCompany(companyResponse);
        return response;
    }

    @Override
    public List<InternshipResponse> getAllInternships() {
        return internshipRepository.findAll().stream()
                .map(internship -> {
                    InternshipResponse response = internshipMapper.toResponse(internship);
                    Company company = internship.getCompany();
                    CompanyResponse companyResponse = companyMapper.toResponse(company);
                    response.setCompany(companyResponse);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    public InternshipResponse getInternshipById(UUID id) {
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with id: " + id));
        InternshipResponse response = internshipMapper.toResponse(internship);
        Company company = internship.getCompany();
        CompanyResponse companyResponse = companyMapper.toResponse(company);

        response.setCompany(companyResponse);

        return response;
    }

    @Override
    public List<InternshipResponse> getInternshipsByCompanyId(UUID companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + companyId));

        CompanyResponse companyResponse = companyMapper.toResponse(company);

        return internshipRepository.findByCompanyId(companyId).stream()
                .map(internship -> {
                    InternshipResponse response = internshipMapper.toResponse(internship);
                    response.setCompany(companyResponse);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteInternship(UUID id) {
        if (!internshipRepository.existsById(id)) {
            throw new ResourceNotFoundException("Internship not found with id: " + id);
        }
        internshipRepository.deleteById(id);
    }

    @Override
    @Transactional
    public InternshipResponse setInternshipClosed(UUID id, boolean closed) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Internship internship = internshipRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Internship not found with id: " + id));

        if (authentication == null || !internship.getCompany().getEmail().equals(authentication.getName())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only the company that posted this internship can change its status");
        }

        internship.setClosed(closed);
        Internship savedInternship = internshipRepository.save(internship);
        InternshipResponse response = internshipMapper.toResponse(savedInternship);
        response.setCompany(companyMapper.toResponse(savedInternship.getCompany()));
        return response;
    }

    @Override
    public Page<InternshipResponse> searchInternships(String title, String city, List<InternshipType> types,
            List<WorkMode> workModes, String paid, Pageable pageable) {

        Specification<Internship> spec = InternshipSpecification.searchInternships(
                title, city, types, workModes, paid);

        return internshipRepository.findAll(spec, pageable).map(
                (internship) -> {
                    InternshipResponse response = internshipMapper.toResponse(internship);
                    Company company = internship.getCompany();
                    CompanyResponse companyResponse = companyMapper.toResponse(company);
                    response.setCompany(companyResponse);
                    return response;
                });
    }
}
