package lv.gderuki.studytracker.controller;

import lombok.RequiredArgsConstructor;
import lv.gderuki.studytracker.dto.StudyTopicDto;
import lv.gderuki.studytracker.mapper.StudyTopicMapper;
import lv.gderuki.studytracker.service.StudyTopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/")
public class StudyTopicController {

    private final StudyTopicService service;
    private final StudyTopicMapper mapper;

    @GetMapping(path = "/topics")
    public List<StudyTopicDto> getAllStudyTopics() {
        return service.getAllStudyTopics().stream().map(mapper::mapEntityToDto).toList();
    }

    @GetMapping("/topics/{id}")
    public ResponseEntity<StudyTopicDto> getStudyTopicById(@PathVariable("id") Long id) {
        return ResponseEntity.of(service.getStudyTopicById(id).map(mapper::mapEntityToDto));
    }

}
