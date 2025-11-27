package vv.pms.presentation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.availability.Availability;
import vv.pms.availability.AvailabilityService;
import vv.pms.allocation.AllocationService;
import vv.pms.allocation.ProjectAllocation;
import vv.pms.presentation.internal.PresentationSlotRepository;
import vv.pms.presentation.internal.RoomRepository;
import vv.pms.professor.ProfessorService;
import vv.pms.professor.Professor;
import vv.pms.project.Project;
import vv.pms.project.ProjectService;
import vv.pms.student.Student;
import vv.pms.student.StudentService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PresentationService {

    private final RoomRepository roomRepository;
    private final PresentationSlotRepository slotRepository;
    private final AvailabilityService availabilityService;
    private final AllocationService allocationService;
    private final ProjectService projectService;
    private final ProfessorService professorService;
    private final StudentService studentService;

    private static final int DAYS = 5;
    private static final int BINS = 32;
    private static final int DURATION_BINS = 2; // 30 minutes

    private static final String[] DAY_NAMES = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
    };

    public record SlotOption(int dayIndex, int startBinIndex, String label) {}

    public PresentationService(RoomRepository roomRepository,
                               PresentationSlotRepository slotRepository,
                               AvailabilityService availabilityService,
                               AllocationService allocationService,
                               ProjectService projectService,
                               ProfessorService professorService,
                               StudentService studentService) {
        this.roomRepository = roomRepository;
        this.slotRepository = slotRepository;
        this.availabilityService = availabilityService;
        this.allocationService = allocationService;
        this.projectService = projectService;
        this.professorService = professorService;
        this.studentService = studentService;
    }

    // ----------------------------------------------------
    // Presentation assignment
    // ----------------------------------------------------

    public PresentationSlot assignPresentation(Long projectId,
                                               Long roomId,
                                               int dayIndex,
                                               int startBinIndex) {
        if (dayIndex < 0 || dayIndex >= DAYS) {
            throw new IllegalArgumentException("Invalid day index: " + dayIndex);
        }
        if (startBinIndex < 0 || startBinIndex >= BINS - (DURATION_BINS - 1)) {
            throw new IllegalArgumentException("Invalid start-bin index: " + startBinIndex);
        }

        Project project = projectService.findProjectById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project " + projectId + " not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room " + roomId + " not found"));

        // Prevent overlapping presentations in the same room at the same time
        if (hasRoomConflict(roomId, dayIndex, startBinIndex, DURATION_BINS, projectId)) {
            throw new IllegalStateException("Room is already booked at that time.");
        }

        PresentationSlot slot = slotRepository.findByProjectId(projectId)
                .orElseGet(PresentationSlot::new);

        slot.setProjectId(project.getId());
        slot.setRoomId(room.getId());
        slot.setDayIndex(dayIndex);
        slot.setStartBinIndex(startBinIndex);
        slot.setDurationBins(DURATION_BINS);

        return slotRepository.save(slot);
    }

    public void unassignPresentation(Long projectId) {
        slotRepository.findByProjectId(projectId).ifPresent(slotRepository::delete);
    }

    @Transactional(readOnly = true)
    public Optional<PresentationSlot> findByProjectId(Long projectId) {
        return slotRepository.findByProjectId(projectId);
    }

    // ----------------------------------------------------
    // Available slots for a given project + room
    // ----------------------------------------------------

    @Transactional(readOnly = true)
    public List<SlotOption> getAvailableSlots(Long projectId, Long roomId) {
        // Need allocation: project -> professor + students
        ProjectAllocation allocation = allocationService.findAllocationByProjectId(projectId)
                .orElse(null);
        if (allocation == null || allocation.getAssignedStudentIds().isEmpty()) {
            return List.of();
        }

        // Room availability
        Room room = roomRepository.findById(roomId)
                .orElse(null);
        if (room == null) {
            return List.of();
        }
        Boolean[][] roomAvail = normalize(room.getAvailability());

        // Professor availability
        Long professorId = allocation.getProfessorId();
        Professor prof = professorService.findProfessorById(professorId)
                .orElse(null);
        if (prof == null) {
            return List.of();
        }
        Availability profAvailEntity = availabilityService.getAvailability(professorId, "PROFESSOR");
        Boolean[][] profAvail = normalize(profAvailEntity.getTimeslots());

        // Students availability
        List<Long> studentIds = allocation.getAssignedStudentIds();
        List<Boolean[][]> studentMatrices = new ArrayList<>();
        for (Long sid : studentIds) {
            Student s = studentService.findStudentById(sid).orElse(null);
            if (s == null) continue;
            Availability sAvailEntity = availabilityService.getAvailability(sid, "STUDENT");
            studentMatrices.add(normalize(sAvailEntity.getTimeslots()));
        }

        // Global intersection: room ∧ professor ∧ all students
        boolean[][] intersection = new boolean[DAYS][BINS];
        for (int d = 0; d < DAYS; d++) {
            for (int t = 0; t < BINS; t++) {
                boolean ok = bool(roomAvail[d][t]) && bool(profAvail[d][t]);
                for (Boolean[][] sm : studentMatrices) {
                    ok = ok && bool(sm[d][t]);
                    if (!ok) break;
                }
                intersection[d][t] = ok;
            }
        }

        // Block times where room already has another presentation
        Map<Integer, Set<Integer>> occupied = occupiedBinsForRoom(roomId, projectId);

        List<SlotOption> result = new ArrayList<>();
        for (int d = 0; d < DAYS; d++) {
            for (int t = 0; t <= BINS - DURATION_BINS; t++) {
                if (!intersection[d][t] || !intersection[d][t + 1]) {
                    continue;
                }
                if (binRangeOverlaps(occupied, d, t, DURATION_BINS)) {
                    continue;
                }
                String label = formatSlotLabel(d, t, DURATION_BINS);
                result.add(new SlotOption(d, t, label));
            }
        }

        // Sort by day, then time
        return result.stream()
                .sorted(Comparator
                        .comparingInt(SlotOption::dayIndex)
                        .thenComparingInt(SlotOption::startBinIndex))
                .collect(Collectors.toList());
    }

    private Boolean[][] normalize(Boolean[][] src) {
        Boolean[][] matrix = new Boolean[DAYS][BINS];
        for (int d = 0; d < DAYS; d++) {
            for (int t = 0; t < BINS; t++) {
                Boolean v = (src != null && d < src.length && src[d] != null && t < src[d].length)
                        ? src[d][t]
                        : Boolean.FALSE;
                matrix[d][t] = (v != null ? v : Boolean.FALSE);
            }
        }
        return matrix;
    }

    private boolean bool(Boolean b) {
        return b != null && b;
    }

    private Map<Integer, Set<Integer>> occupiedBinsForRoom(Long roomId, Long projectIdToIgnore) {
        List<PresentationSlot> slots = slotRepository.findByRoomId(roomId);
        Map<Integer, Set<Integer>> map = new HashMap<>();
        for (PresentationSlot s : slots) {
            if (projectIdToIgnore != null && projectIdToIgnore.equals(s.getProjectId())) {
                continue; // let this project move its own slot
            }
            int d = s.getDayIndex();
            int start = s.getStartBinIndex();
            int dur = s.getDurationBins();
            Set<Integer> set = map.computeIfAbsent(d, k -> new HashSet<>());
            for (int t = start; t < start + dur; t++) {
                set.add(t);
            }
        }
        return map;
    }

    private boolean binRangeOverlaps(Map<Integer, Set<Integer>> occupied,
                                     int dayIndex,
                                     int startBin,
                                     int dur) {
        Set<Integer> used = occupied.get(dayIndex);
        if (used == null) return false;
        for (int t = startBin; t < startBin + dur; t++) {
            if (used.contains(t)) return true;
        }
        return false;
    }

    private boolean hasRoomConflict(Long roomId, int dayIndex, int startBinIndex, int dur, Long projectId) {
        Map<Integer, Set<Integer>> occupied = occupiedBinsForRoom(roomId, projectId);
        return binRangeOverlaps(occupied, dayIndex, startBinIndex, dur);
    }

    private String formatSlotLabel(int dayIndex, int startBinIndex, int durBins) {
        String day = DAY_NAMES[dayIndex];
        int startMinutes = 8 * 60 + startBinIndex * 15;
        int endMinutes = startMinutes + durBins * 15;

        return String.format(
                "%s %02d:%02d-%02d:%02d",
                day,
                startMinutes / 60, startMinutes % 60,
                endMinutes / 60, endMinutes % 60
        );
    }

    // ----------------------------------------------------
    // Best-effort allocation
    // ----------------------------------------------------

    public void runBestEffortAllocation() {
        List<Room> rooms = roomRepository.findAll();
        if (rooms.isEmpty()) {
            return; // nothing to do
        }

        List<ProjectAllocation> allocations = allocationService.findAllAllocations().stream()
                .filter(a -> !a.getAssignedStudentIds().isEmpty())
                .collect(Collectors.toList());

        // only projects that exist and don't already have presentation slot
        Set<Long> projectIds = allocations.stream()
                .map(ProjectAllocation::getProjectId)
                .collect(Collectors.toSet());

        for (Long pid : projectIds) {
            if (slotRepository.findByProjectId(pid).isPresent()) {
                continue; // already assigned
            }

            // Try rooms in order, pick first available slot for each project
            for (Room room : rooms) {
                List<SlotOption> options = getAvailableSlots(pid, room.getId());
                if (!options.isEmpty()) {
                    SlotOption first = options.get(0);
                    try {
                        assignPresentation(pid, room.getId(), first.dayIndex(), first.startBinIndex());
                        break; // move to next project
                    } catch (RuntimeException ignored) {
                        // best-effort: keep trying other rooms if this one fails
                    }
                }
            }
        }
    }
}

