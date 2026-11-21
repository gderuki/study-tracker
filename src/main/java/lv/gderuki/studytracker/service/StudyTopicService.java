package lv.gderuki.studytracker.service;

import lv.gderuki.studytracker.entity.StudyTopic;
import lv.gderuki.studytracker.repository.StudyTopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudyTopicService {

    private final StudyTopicRepository repository;

    public StudyTopicService(StudyTopicRepository repository) {
        this.repository = repository;
    }

    public List<StudyTopic> getAllStudyTopics() {
        return repository.findAll();
    }

    public Optional<StudyTopic> getStudyTopicById(Long id) {
        return repository.findById(id);
    }

}
