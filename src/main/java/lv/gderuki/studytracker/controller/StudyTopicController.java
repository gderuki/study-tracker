package lv.gderuki.studytracker.controller;

import lv.gderuki.studytracker.entity.StudyTopic;
import lv.gderuki.studytracker.service.StudyTopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StudyTopicController {

    private final StudyTopicService service;

    public StudyTopicController(StudyTopicService service) {
        this.service = service;
    }

    @GetMapping(path = "/api/topics")
    public ResponseEntity<List<StudyTopic>> getAllStudyTopics() {
        return ResponseEntity.ok(service.getAllStudyTopics());
    }

    @GetMapping("/api/topics/{id}")
    public ResponseEntity<StudyTopic> getStudyTopicById(
            @PathVariable("id") Long id) {
        return ResponseEntity.of(service.getStudyTopicById(id));
    }

}
