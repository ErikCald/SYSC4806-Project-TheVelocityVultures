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
        //create the Professor FIRST (We need the ID)
        Professor professor = professorService.addProfessor("Prof Integration", "integration@example.com");

        Project project = projectService.addProject(
                "Integration Test Project",
                "Test Description",
                Set.of(Program.SOFTWARE_ENGINEERING),
                2,
                professor.getId() //added the required 5th argument
        );

        //verify allocation automatic
        ProjectAllocation allocation = allocationRepository.findByProjectId(project.getId())
                .orElseThrow(() -> new AssertionError("Allocation should have been created by addProject"));
        assertEquals(professor.getId(), allocation.getProfessorId());

        //Attempting to assign the same professor again should throw the exception
        assertThrows(AllocationService.AllocationStateException.class, () ->
                allocationService.assignProfessorToProject(project.getId(), professor.getId())
        );
    }
}