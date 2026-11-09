package ma.ensa.internHub.controllers;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import ma.ensa.internHub.domain.dto.request.CompanyUpdateRequest;
import ma.ensa.internHub.domain.dto.request.NotificationRequest;
import ma.ensa.internHub.domain.dto.response.CompanyResponse;
import ma.ensa.internHub.services.CompanyService;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/count-by-month")
    public Map<String, Long> countCompaniesByMonth() {
        return companyService.countCompaniesByMonth();
    }

    @GetMapping("/count")
    public long countAllCompanies() {
        return companyService.countCompanies();
    }

    @DeleteMapping("/{id}")
    public void deleteCompanyById(@PathVariable UUID id) {
        companyService.deleteCompanyById(id);
    }

    @GetMapping
    public List<CompanyResponse> getAllCompanies() {
        return companyService.getAllCompanies();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable UUID id,
            @Valid @RequestBody CompanyUpdateRequest request) {
        return ResponseEntity.ok(companyService.updateCompany(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<CompanyResponse> getCompanyByEmail(@PathVariable String email) {
        return ResponseEntity.ok(companyService.getCompanyByEmail(email));
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<Void> blockCompany(
            @PathVariable UUID id,
            @ModelAttribute NotificationRequest request) {
        companyService.blockCompany(id, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<Void> unblockCompany(
            @PathVariable UUID id,
            @ModelAttribute NotificationRequest request) {
        companyService.unblockCompany(id, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count-blocked")
    public long getBlockedCompaniesCount() {
        return companyService.countBlockedCompanies();
    }
}
