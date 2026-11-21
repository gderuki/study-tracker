package lv.gderuki.studytracker.service;

import lombok.RequiredArgsConstructor;
import lv.gderuki.studytracker.entity.StudyTopic;
import lv.gderuki.studytracker.repository.StudyTopicRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class StudyTopicService {

    private final StudyTopicRepository repository;

    public List<StudyTopic> getAllStudyTopics() {
        return repository.findAll();
    }

    public Optional<StudyTopic> getStudyTopicById(Long id) {
        return repository.findById(id);
    }

}
