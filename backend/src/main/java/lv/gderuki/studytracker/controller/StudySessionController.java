package lv.gderuki.studytracker.controller;

import lombok.AllArgsConstructor;
import lv.gderuki.studytracker.entity.StudySession;
import lv.gderuki.studytracker.service.StudySessionService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class StudySessionController {

    private final StudySessionService studySessionService;

    @PostMapping("/api/sessions")
    public StudySession createSessionByTopicId(@RequestBody Long topicId) {
        return studySessionService.startSession(topicId);
    }

}
