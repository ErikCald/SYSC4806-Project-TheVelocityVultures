package org.velocity.vultures;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "students", uniqueConstraints = {
        @UniqueConstraint(name = "uk_student_student_id", columnNames = "studentId"),
        @UniqueConstraint(name = "uk_student_email", columnNames = "email")
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String studentId; // University ID

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Program program;

    @Column(nullable = false)
    private boolean hasProject = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_topic_id")
    private ProjectTopic projectTopic;

    public Student() {}

    public Student(String name, String studentId, String email, Program program) {
        this.name = name;
        this.studentId = studentId;
        this.email = email;
        this.program = program;
        this.hasProject = false;
    }

    public Long getId() { return id;
    }

    public String getName() { return name;
    }

    public void setName(String name) { this.name = name;
    }

    public String getStudentId() { return studentId;
    }

    public void setStudentId(String studentId) { this.studentId = studentId;
    }

    public String getEmail() { return email;
    }

    public void setEmail(String email) { this.email = email;
    }

    public Program getProgram() { return program;
    }

    public void setProgram(Program program) { this.program = program;
    }

    public boolean isHasProject() { return hasProject;
    }

    public void setHasProject(boolean hasProject) { this.hasProject = hasProject; }


    public ProjectTopic getProjectTopic() { return projectTopic;
    }

    public void setProjectTopic(ProjectTopic projectTopic) { this.projectTopic = projectTopic;
    }

}
