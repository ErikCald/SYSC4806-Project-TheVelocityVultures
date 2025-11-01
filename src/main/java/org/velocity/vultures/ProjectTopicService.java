package org.velocity.vultures;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectTopicService {

    private final ProjectTopicRepository topicRepo;
    private final StudentRepository studentRepo;

    public ProjectTopicService(ProjectTopicRepository topicRepo, StudentRepository studentRepo) {
        this.topicRepo = topicRepo;
        this.studentRepo = studentRepo;
    }

    @Transactional
    public void applyToTopic(Long studentId, Long topicId) {
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        ProjectTopic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));

        if (student.isHasProject()) {
            throw new IllegalStateException("Student already assigned to a project");
        }
        if (topic.getStatus() == ProjectStatus.ARCHIVED) {
            throw new IllegalStateException("Topic is archived");
        }
        if (topic.isFull()) {
            throw new IllegalStateException("Topic is full");
        }
        if (!topic.acceptsProgram(student.getProgram())) {
            throw new IllegalStateException("Student program not allowed for this topic");
        }

        student.setProjectTopic(topic);
        student.setHasProject(true);
        studentRepo.save(student);

        topic.refreshStatus();
        topicRepo.save(topic);
    }

    @Transactional
    public void archiveTopic(Long topicId) {
        ProjectTopic topic = topicRepo.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        topic.setStatus(ProjectStatus.ARCHIVED);
        topicRepo.save(topic);
    }
}
