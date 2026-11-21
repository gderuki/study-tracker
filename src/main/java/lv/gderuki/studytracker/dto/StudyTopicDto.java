package lv.gderuki.studytracker.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class StudyTopicDto {
    @Getter
    private Long id;
    @Getter
    private String title;
    @Getter
    private String category;
    @Getter
    private String description;
    @Getter
    private boolean isNiceToHave;
}
