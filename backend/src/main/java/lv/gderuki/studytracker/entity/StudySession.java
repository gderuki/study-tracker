package lv.gderuki.studytracker.entity;

import jakarta.persistence.*;
import lombok.*;
import lv.gderuki.studytracker.exception.InvalidSessionStateException;

import java.time.Instant;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudySession {
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Instant startTime;
    private Instant endTime;
    @ManyToOne(optional = false)
    @JoinColumn(name = "topic_id", nullable = false)
    private StudyTopic topic;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status;

    public void completeSession(Instant completedAt) {
        if (this.status != SessionStatus.STARTED) {
            throw new InvalidSessionStateException("Only a started session can be completed");
        }

        if (completedAt == null || completedAt.isBefore(this.startTime)) {
            throw new IllegalArgumentException(
                    "Completion time must not be null or before start time");
        }

        this.status = SessionStatus.COMPLETED;
        this.endTime = completedAt;
    }
}
