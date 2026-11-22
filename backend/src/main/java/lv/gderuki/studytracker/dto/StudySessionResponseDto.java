package lv.gderuki.studytracker.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lv.gderuki.studytracker.entity.SessionStatus;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class StudySessionResponseDto {

    private Long id;
    private Instant startTime;
    private Instant endTime;
    private SessionStatus status;
    private StudyTopicResponseDto studyTopic;

}
