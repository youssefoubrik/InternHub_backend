package ma.ensa.internHub.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.ensa.internHub.domain.dto.request.InternshipRequest;
import ma.ensa.internHub.domain.dto.response.InternshipResponse;
import ma.ensa.internHub.domain.entities.InternshipType;
import ma.ensa.internHub.domain.entities.WorkMode;
import ma.ensa.internHub.services.InternshipService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1/internships")
@RequiredArgsConstructor
public class InternshipController {

    private final InternshipService internshipService;

    @GetMapping("/count/remote")
    public long countRemoteInternships() {
        return internshipService.countInternshipsByWorkMode(WorkMode.REMOTE);
    }

    @GetMapping("/count/on-site")
    public long countOnSiteInternships() {
        return internshipService.countInternshipsByWorkMode(WorkMode.ON_SITE);
    }

    @GetMapping("/count")
    public long countAllInternships() {
        return internshipService.countAllInternships();
    }

    @GetMapping("/count/company/{companyId}") // added
    public ResponseEntity<Long> countAllCompanyInternships(@PathVariable UUID companyId) {
        return ResponseEntity.ok(internshipService.countInternshipsByCompanyId(companyId));
    }

    @PostMapping
    public ResponseEntity<InternshipResponse> createInternship(@Valid @RequestBody InternshipRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(internshipService.saveInternship(request));
    }

    @GetMapping
    public ResponseEntity<List<InternshipResponse>> getAllInternships() {
        return ResponseEntity.ok(internshipService.getAllInternships());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InternshipResponse> getInternshipById(@PathVariable UUID id) {
        return ResponseEntity.ok(internshipService.getInternshipById(id));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<InternshipResponse>> getInternshipsByCompany(@PathVariable UUID companyId) {
        return ResponseEntity.ok(internshipService.getInternshipsByCompanyId(companyId));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<InternshipResponse>> searchInternships(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) List<InternshipType> types,
            @RequestParam(required = false) List<WorkMode> workModes,
            @RequestParam(required = false) String paid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "40") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(internshipService.searchInternships(title, city, types, workModes, paid, pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInternship(@PathVariable UUID id) {
        internshipService.deleteInternship(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<InternshipResponse> closeInternship(@PathVariable UUID id) {
        return ResponseEntity.ok(internshipService.setInternshipClosed(id, true));
    }

    @PutMapping("/{id}/open")
    public ResponseEntity<InternshipResponse> openInternship(@PathVariable UUID id) {
        return ResponseEntity.ok(internshipService.setInternshipClosed(id, false));
    }

}
