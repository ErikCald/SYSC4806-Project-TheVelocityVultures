package vv.pms.presentation.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import vv.pms.presentation.Room;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByName(String name);
}
