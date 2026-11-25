package lv.gderuki.studytracker.entity;

import lombok.Getter;
import lv.gderuki.studytracker.exception.InvalidStageStateException;

import java.time.Duration;
import java.time.Instant;

@Getter
public class StudySessionStage {

    private final Instant startedAt;
    private Instant pausedAt;
    private Duration accumulatedPause = Duration.ZERO;
    private StudySessionStageStatus status = StudySessionStageStatus.ONGOING;

    public StudySessionStage(Instant startedAt) {
        if (startedAt == null) {
            throw new IllegalArgumentException("Start time must not be null");
        }
        this.startedAt = startedAt;
    }

    public void pause(Instant now) {
        if (this.status != StudySessionStageStatus.ONGOING) {
            throw new InvalidStageStateException("Only an ongoing stage can be paused");
        }

        if (now == null || now.isBefore(startedAt)) {
            throw new IllegalArgumentException("Pause time must not be null or before start time");
        }

        this.pausedAt = now;
        this.status = StudySessionStageStatus.PAUSED;
    }

    public void resume(Instant now) {
        if (this.status != StudySessionStageStatus.PAUSED) {
            throw new InvalidStageStateException("Only a paused stage can be resumed");
        }

        if (now == null || now.isBefore(pausedAt)) {
            throw new IllegalArgumentException("Resume time must not be null or before pause time");
        }

        var currentPause = Duration.between(pausedAt, now);
        this.accumulatedPause = accumulatedPause.plus(currentPause);
        this.status = StudySessionStageStatus.ONGOING;
        this.pausedAt = null;
    }
}
