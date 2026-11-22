package lv.gderuki.studytracker.controller;

import lombok.AllArgsConstructor;
import lv.gderuki.studytracker.dto.StudySessionRequestDto;
import lv.gderuki.studytracker.dto.StudySessionResponseDto;
import lv.gderuki.studytracker.mapper.StudyTrackerMapper;
import lv.gderuki.studytracker.service.StudySessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/")
public class StudySessionController {

    private final StudySessionService studySessionService;
    private final StudyTrackerMapper mapper;

    @PostMapping("/sessions")
    public ResponseEntity<StudySessionResponseDto> createSessionByTopicId(@RequestBody StudySessionRequestDto request) {
        var session = studySessionService.startSession(request.getTopicId());
        var response = mapper.mapStudySessionEntityToDto(session);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

}
