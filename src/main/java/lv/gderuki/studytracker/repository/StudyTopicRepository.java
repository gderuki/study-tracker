package lv.gderuki.studytracker.repository;

import lv.gderuki.studytracker.entity.StudyTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyTopicRepository extends JpaRepository<StudyTopic, Long> {

    List<StudyTopic> findAllByIsNiceToHave(Boolean isNiceToHave);

}
