package vv.pms;

import vv.pms.allocation.AllocationService;
import vv.pms.allocation.ProjectAllocation;
import vv.pms.professor.Professor;
import vv.pms.project.ProjectService;
import vv.pms.student.StudentService;
import vv.pms.professor.ProfessorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import vv.pms.allocation.internal.ProjectAllocationRepository;
import vv.pms.project.Project;
import vv.pms.project.Program;
import vv.pms.student.Student;
import vv.pms.student.StudentService;
import vv.pms.professor.ProfessorService;
import vv.pms.allocation.internal.ProjectAllocationRepository;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllocationServiceTest {

    @Mock
    private ProjectAllocationRepository repository;
    @Mock
    private ProjectService projectService;
    @Mock
    private StudentService studentService;
    @Mock
    private ProfessorService professorService;

    @InjectMocks
    private AllocationService allocationService;

    private Project project;
    private Student student;
    private final Long projectId = 1L;
    private final Long professorId = 10L;
    private final Long studentId = 100L;

    @BeforeEach
    void setup() {
        project = new Project("Test Project", "Desc", Set.of(Program.SOFTWARE_ENGINEERING), 2);
        project.setId(projectId);
        student = new Student("Alice", "S100", "alice@test.com", Program.SOFTWARE_ENGINEERING);
        student.setId(studentId);
    }

    // --- Professor allocation tests ---
    @Test
    void assignProfessorToProject_success() {
        when(projectService.findProjectById(projectId)).thenReturn(Optional.of(project));
        when(professorService.findProfessorById(professorId)).thenReturn(Optional.of(mock(vv.pms.professor.Professor.class)));
        when(repository.findByProjectId(projectId)).thenReturn(Optional.empty());
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        when(repository.save(any())).thenReturn(allocation);

        ProjectAllocation result = allocationService.assignProfessorToProject(projectId, professorId);

        assertEquals(projectId, result.getProjectId());
        assertEquals(professorId, result.getProfessorId());
    }

    @Test
    void assignProfessorToProject_duplicate_throws() {
        // Arrange: manually create Project and Professor objects
        Project project = new Project(
                "Test Project",
                "Description",
                Set.of(Program.SOFTWARE_ENGINEERING),
                2
        );
        project.setId(1L); // fake ID just for consistency

        Professor professor = new Professor("Prof X", "profX@example.com");
        professor.setId(10L); // fake ID

        // Mock ProjectService to return the project
        ProjectService mockProjectService = Mockito.mock(ProjectService.class);
        Mockito.when(mockProjectService.findProjectById(project.getId()))
                .thenReturn(Optional.of(project));

        // Mock ProfessorService to return the professor
        ProfessorService mockProfessorService = Mockito.mock(ProfessorService.class);
        Mockito.when(mockProfessorService.findProfessorById(professor.getId()))
                .thenReturn(Optional.of(professor));

        // Use a simple in-memory map for allocations (or just rely on the real AllocationService methods)
        ProjectAllocationRepository repo = Mockito.mock(ProjectAllocationRepository.class);
        Mockito.when(repo.findByProjectId(project.getId()))
                .thenReturn(Optional.empty())  // first allocation
                .thenReturn(Optional.of(new ProjectAllocation(project.getId(), professor.getId()))); // second allocation

        AllocationService allocationService = new AllocationService(
                repo,
                mockProfessorService,
                mockProjectService,
                Mockito.mock(StudentService.class) // not needed for this test
        );

        // Act: first allocation succeeds
        allocationService.assignProfessorToProject(project.getId(), professor.getId());

        // Assert: second allocation should throw AllocationStateException
        assertThrows(AllocationService.AllocationStateException.class, () -> {
            allocationService.assignProfessorToProject(project.getId(), professor.getId());
        });
    }

    @Test
    void removeProfessorAllocation_success() {
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        when(repository.findByProjectId(projectId)).thenReturn(Optional.of(allocation));
        doNothing().when(repository).delete(allocation);

        assertDoesNotThrow(() -> allocationService.removeProfessorAllocation(projectId));
        verify(repository).delete(allocation);
    }

    @Test
    void removeProfessorAllocation_notFound_throws() {
        when(repository.findByProjectId(projectId)).thenReturn(Optional.empty());
        assertThrows(AllocationService.AllocationNotFoundException.class,
                () -> allocationService.removeProfessorAllocation(projectId));
    }

    // --- Student allocation tests ---
    @Test
    void assignStudentToProject_success() {
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        when(repository.findByProjectId(projectId)).thenReturn(Optional.of(allocation));
        when(projectService.findProjectById(projectId)).thenReturn(Optional.of(project));
        when(studentService.findStudentById(studentId)).thenReturn(Optional.of(student));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectAllocation result = allocationService.assignStudentToProject(projectId, studentId);

        assertTrue(result.getAssignedStudentIds().contains(studentId));
        verify(studentService).updateProjectStatus(studentId, true);
    }

    @Test
    void assignStudent_duplicateStudent_throws() {
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        allocation.assignStudent(studentId);
        when(repository.findByProjectId(projectId)).thenReturn(Optional.of(allocation));
        when(projectService.findProjectById(projectId)).thenReturn(Optional.of(project));
        when(studentService.findStudentById(studentId)).thenReturn(Optional.of(student));

        assertThrows(AllocationService.AllocationStateException.class,
                () -> allocationService.assignStudentToProject(projectId, studentId));
    }

    @Test
    void assignStudent_alreadyHasProject_throws() {
        student.setHasProject(true);
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        when(repository.findByProjectId(projectId)).thenReturn(Optional.of(allocation));
        when(projectService.findProjectById(projectId)).thenReturn(Optional.of(project));
        when(studentService.findStudentById(studentId)).thenReturn(Optional.of(student));

        assertThrows(AllocationService.AllocationStateException.class,
                () -> allocationService.assignStudentToProject(projectId, studentId));
    }

    @Test
    void assignStudent_projectFull_throws() {
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        allocation.assignStudent(Long.valueOf(200L));
        allocation.assignStudent(Long.valueOf(201L)); // project requires 2 students
        when(repository.findByProjectId(projectId)).thenReturn(Optional.of(allocation));
        when(projectService.findProjectById(projectId)).thenReturn(Optional.of(project));
        when(studentService.findStudentById(studentId)).thenReturn(Optional.of(student));

        assertThrows(AllocationService.AllocationStateException.class,
                () -> allocationService.assignStudentToProject(projectId, studentId));
    }

    @Test
    void assignStudent_programMismatch_throws() {
        student.setProgram(Program.COMPUTER_SYSTEMS_ENGINEERING);
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        when(repository.findByProjectId(projectId)).thenReturn(Optional.of(allocation));
        when(projectService.findProjectById(projectId)).thenReturn(Optional.of(project));
        when(studentService.findStudentById(studentId)).thenReturn(Optional.of(student));

        assertThrows(AllocationService.AllocationStateException.class,
                () -> allocationService.assignStudentToProject(projectId, studentId));
    }

    @Test
    void unassignStudentFromProject_success() {
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        allocation.assignStudent(studentId);
        when(repository.findByProjectId(projectId)).thenReturn(Optional.of(allocation));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProjectAllocation result = allocationService.unassignStudentFromProject(projectId, studentId);

        assertFalse(result.getAssignedStudentIds().contains(studentId));
        verify(studentService).updateProjectStatus(studentId, false);
    }

    @Test
    void unassignStudent_notAssigned_throws() {
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        when(repository.findByProjectId(projectId)).thenReturn(Optional.of(allocation));

        assertThrows(AllocationService.AllocationNotFoundException.class,
                () -> allocationService.unassignStudentFromProject(projectId, studentId));
    }
}