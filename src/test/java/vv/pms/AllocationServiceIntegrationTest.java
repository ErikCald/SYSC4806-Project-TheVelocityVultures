package vv.pms;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.allocation.AllocationService;
import vv.pms.allocation.ProjectAllocation;
import vv.pms.allocation.internal.ProjectAllocationRepository;
import vv.pms.project.Project;
import vv.pms.project.ProjectService;
import vv.pms.project.Program;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AllocationServiceIntegrationTest {

    @Autowired
    private AllocationService allocationService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProfessorService professorService;

    @Autowired
    private ProjectAllocationRepository allocationRepository;

    @Test
    void assignProfessorToProject_duplicate_throws() {
        // Arrange
        Project project = projectService.addProject(
                "Integration Test Project",
                "Test Description",
                Set.of(Program.SOFTWARE_ENGINEERING),
                2
        );

        Professor professor = professorService.addProfessor("Prof Integration", "integration@example.com");

        // First allocation succeeds
        allocationService.assignProfessorToProject(project.getId(), professor.getId());

        // Reload allocation from DB to verify
        ProjectAllocation allocation = allocationRepository.findByProjectId(project.getId())
                .orElseThrow(() -> new AssertionError("Allocation should exist"));
        assertEquals(professor.getId(), allocation.getProfessorId());

        // Second allocation should throw
        assertThrows(AllocationService.AllocationStateException.class, () ->
                allocationService.assignProfessorToProject(project.getId(), professor.getId())
        );
    }
}