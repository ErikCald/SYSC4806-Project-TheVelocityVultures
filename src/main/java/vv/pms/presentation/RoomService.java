package vv.pms.presentation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.presentation.internal.RoomRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomService {

    private final RoomRepository repository;

    public RoomService(RoomRepository repository) {
        this.repository = repository;
    }

    public Room createRoom(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room name is required.");
        }
        repository.findByName(name.trim()).ifPresent(r -> {
            throw new IllegalArgumentException("Room with this name already exists.");
        });
        Room room = new Room(name.trim());
        return repository.save(room);
    }

    public void updateRoom(Long id, String name) {
        Room room = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room with id " + id + " not found."));
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room name is required.");
        }
        room.setName(name.trim());
        repository.save(room);
    }

    public void deleteRoom(Long id) {
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Room> getAllRooms() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Room> getRoomById(Long id) {
        return repository.findById(id);
    }
}

