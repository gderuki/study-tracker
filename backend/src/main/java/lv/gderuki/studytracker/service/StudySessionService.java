package lv.gderuki.studytracker.service;

import lombok.RequiredArgsConstructor;
import lv.gderuki.studytracker.entity.SessionStatus;
import lv.gderuki.studytracker.entity.StudySession;
import lv.gderuki.studytracker.exception.ActiveSessionAlreadyExistsException;
import lv.gderuki.studytracker.exception.TopicNotFoundException;
import lv.gderuki.studytracker.repository.StudySessionRepository;
import lv.gderuki.studytracker.repository.StudyTopicRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Service
public class StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final StudyTopicRepository studyTopicRepository;

    // start session method
    public StudySession startSession(Long topicId) {
        var studyTopic = studyTopicRepository.findById(topicId).orElseThrow(() -> new TopicNotFoundException("Topic doesn't exist!"));

        if (studySessionRepository.existsByStatus(SessionStatus.STARTED)) {
            throw new ActiveSessionAlreadyExistsException("Session already exists!");
        }

        return studySessionRepository.save(new StudySession(
                null,
                Instant.now(),
                null,
                studyTopic,
                SessionStatus.STARTED
        ));
    }

}
