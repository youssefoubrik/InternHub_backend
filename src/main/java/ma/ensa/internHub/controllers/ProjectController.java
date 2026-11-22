package ma.ensa.internHub.controllers;

import lombok.RequiredArgsConstructor;
import ma.ensa.internHub.domain.dto.request.ProjectRequest;
import ma.ensa.internHub.domain.dto.response.ProjectResponse;
import ma.ensa.internHub.services.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable UUID id) {
        ProjectResponse response = projectService.getProjectById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ProjectResponse>> getProjectsByStudentId(@PathVariable UUID studentId) {
        List<ProjectResponse> responses = projectService.getProjectsByStudentId(studentId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        List<ProjectResponse> responses = projectService.getAllProjects();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable UUID id, @Valid @RequestBody ProjectRequest request) {
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
