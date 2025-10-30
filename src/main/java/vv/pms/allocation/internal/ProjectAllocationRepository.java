package vv.pms.allocation.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import vv.pms.allocation.ProjectAllocation;
import java.util.Optional;

public interface ProjectAllocationRepository extends JpaRepository<ProjectAllocation, Long> {
    Optional<ProjectAllocation> findByProjectId(Long projectId);
}