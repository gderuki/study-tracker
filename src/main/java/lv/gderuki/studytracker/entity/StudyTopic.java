package lv.gderuki.studytracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
public class StudyTopic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Getter
    private String title;
    @Getter
    private String description;
    @Getter
    private String category;
    @Getter
    private Boolean isNiceToHave;

    protected StudyTopic() {
    }

    public StudyTopic(Long id, String title, String description, String category, Boolean isNiceToHave) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.isNiceToHave = isNiceToHave;
    }

    @Override
    public String toString() {
        return String.format("StudyTopic [id=%d]", id); // TODO: get rid of String.format later on
    }
}
