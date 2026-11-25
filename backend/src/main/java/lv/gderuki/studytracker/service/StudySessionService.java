package lv.gderuki.studytracker.service;

import lombok.RequiredArgsConstructor;
import lv.gderuki.studytracker.entity.SessionStatus;
import lv.gderuki.studytracker.entity.StudySession;
import lv.gderuki.studytracker.exception.ActiveSessionAlreadyExistsException;
import lv.gderuki.studytracker.exception.SessionNotFoundException;
import lv.gderuki.studytracker.exception.TopicNotFoundException;
import lv.gderuki.studytracker.repository.StudySessionRepository;
import lv.gderuki.studytracker.repository.StudyTopicRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

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

        StudySession newStudySession;

        try {
            newStudySession = studySessionRepository.save(new StudySession(null, Instant.now(), null, studyTopic, SessionStatus.STARTED));
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException cause
                    && "ux_study_session_single_started".equals(cause.getConstraintName())) {
                throw new ActiveSessionAlreadyExistsException("An active session already exists!");
            }

            throw e;
        }

        return newStudySession;
    }

    public Optional<StudySession> findActiveSession() {
        return studySessionRepository.findStudySessionByStatus(SessionStatus.STARTED);
    }

    public StudySession completeSession(Long sessionId) {
        var session = studySessionRepository.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException("Session not found!"));

        session.completeSession(Instant.now());

        return studySessionRepository.save(session);
    }

}
