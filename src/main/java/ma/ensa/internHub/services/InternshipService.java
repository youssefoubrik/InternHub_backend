package ma.ensa.internHub.services;

import ma.ensa.internHub.domain.dto.request.InternshipRequest;
import ma.ensa.internHub.domain.dto.response.InternshipResponse;
import ma.ensa.internHub.domain.entities.InternshipType;
import ma.ensa.internHub.domain.entities.WorkMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InternshipService {

    long countInternshipsByWorkMode(WorkMode workMode);

    long countAllInternships();

    long countInternshipsByCompanyId(UUID companyId); // added

    InternshipResponse saveInternship(InternshipRequest request);

    List<InternshipResponse> getAllInternships();

    InternshipResponse getInternshipById(UUID id);

    /**
     * Search for internships based on multiple criteria with pagination
     *
     * @param title     Title or skill to search for (partial match, also searches
     *                  in skills)
     * @param city      City to search for (partial match)
     * @param types     List of internship types to filter by
     * @param workModes List of work modes to filter by
     * @param paid      Filter by paid status ("true", "false", or "All")
     * @param pageable  Pagination information
     * @return Page of internships matching the criteria
     */
    Page<InternshipResponse> searchInternships(
            String title,
            String city,
            List<InternshipType> types,
            List<WorkMode> workModes,
            String paid,
            Pageable pageable);

    List<InternshipResponse> getInternshipsByCompanyId(UUID companyId);

    void deleteInternship(UUID id);

    InternshipResponse setInternshipClosed(UUID id, boolean closed);
}
