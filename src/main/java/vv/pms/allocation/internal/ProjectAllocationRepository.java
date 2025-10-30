package vv.pms.allocation.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import vv.pms.allocation.ProjectAllocation;
import java.util.Optional;

public interface ProjectAllocationRepository extends JpaRepository<ProjectAllocation, Long> {

    // Custom finder to check if a project is already allocated
    Optional<ProjectAllocation> findByProjectId(Long projectId);
}