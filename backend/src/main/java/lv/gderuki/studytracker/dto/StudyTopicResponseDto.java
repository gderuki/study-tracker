package lv.gderuki.studytracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudyTopicResponseDto {
    private Long id;
    private String title;
    private String category;
    private String description;
    private boolean isNiceToHave;
}
