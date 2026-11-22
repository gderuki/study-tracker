package lv.gderuki.studytracker.controller;

import lombok.AllArgsConstructor;
import lv.gderuki.studytracker.dto.StudySessionRequestDto;
import lv.gderuki.studytracker.dto.StudySessionResponseDto;
import lv.gderuki.studytracker.mapper.StudyTrackerMapper;
import lv.gderuki.studytracker.service.StudySessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/sessions/active")
    public ResponseEntity<StudySessionResponseDto> getActiveSession() {
        return studySessionService.findActiveSession()
                .map(mapper::mapStudySessionEntityToDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

}
