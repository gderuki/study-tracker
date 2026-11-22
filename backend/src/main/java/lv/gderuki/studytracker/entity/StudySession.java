package lv.gderuki.studytracker.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudySession {
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private Instant startTime;
    private Instant endTime;
    @ManyToOne(optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private StudyTopic topic;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;
}
