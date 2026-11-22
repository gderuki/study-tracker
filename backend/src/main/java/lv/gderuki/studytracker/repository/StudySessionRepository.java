package lv.gderuki.studytracker.repository;

import lv.gderuki.studytracker.entity.SessionStatus;
import lv.gderuki.studytracker.entity.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    boolean existsByStatus(SessionStatus status);

}
