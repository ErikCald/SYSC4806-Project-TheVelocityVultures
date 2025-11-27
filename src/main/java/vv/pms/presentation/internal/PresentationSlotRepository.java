package vv.pms.presentation.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import vv.pms.presentation.PresentationSlot;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PresentationSlotRepository extends JpaRepository<PresentationSlot, Long> {

    Optional<PresentationSlot> findByProjectId(Long projectId);

    List<PresentationSlot> findByRoomId(Long roomId);

    List<PresentationSlot> findByProjectIdIn(Set<Long> projectIds);

    boolean existsByRoomIdAndDayIndexAndStartBinIndex(Long roomId, int dayIndex, int startBinIndex);
}

