package lv.gderuki.studytracker.entity;

import lv.gderuki.studytracker.exception.InvalidStageStateException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class StudySessionStageTest {

    private static final Instant START = Instant.parse("2026-11-25T10:00:00Z");

    @Test
    void shouldAccumulateTwoPauses() {
        var stage = new StudySessionStage(START);
        stage.pause(START.plusSeconds(600));
        assertEquals(StudySessionStageStatus.PAUSED, stage.getStatus());
        assertEquals(START.plusSeconds(600), stage.getPausedAt());
        stage.resume(START.plusSeconds(1500));
        stage.pause(START.plusSeconds(1800));
        stage.resume(START.plusSeconds(2400));

        assertEquals(Duration.ofMinutes(25), stage.getAccumulatedPause());
        assertEquals(START, stage.getStartedAt());
        assertNull(stage.getPausedAt());
        assertEquals(StudySessionStageStatus.ONGOING, stage.getStatus());
    }

    @Test
    void shouldRejectRepeatedPauseWithoutChangingState() {
        var stage = new StudySessionStage(START);
        var pausedAt = START.plusSeconds(600);
        stage.pause(pausedAt);
        assertThrows(InvalidStageStateException.class, () -> stage.pause(pausedAt.plusSeconds(60)));
        assertPaused(stage, pausedAt);
    }

    @Test
    void shouldRejectResumeWhileOngoing() {
        var stage = new StudySessionStage(START);
        assertThrows(InvalidStageStateException.class, () -> stage.resume(START));
        assertOngoing(stage);
    }

    @Test
    void shouldRejectInvalidPauseTimeWithoutChangingState() {
        var stage = new StudySessionStage(START);
        assertThrows(IllegalArgumentException.class, () -> stage.pause(null));
        assertOngoing(stage);
        assertThrows(IllegalArgumentException.class, () -> stage.pause(START.minusNanos(1)));
        assertOngoing(stage);
    }

    @Test
    void shouldRejectInvalidResumeTimeWithoutChangingState() {
        var stage = new StudySessionStage(START);
        var pausedAt = START.plusSeconds(600);
        stage.pause(pausedAt);
        assertThrows(IllegalArgumentException.class, () -> stage.resume(null));
        assertPaused(stage, pausedAt);
        assertThrows(IllegalArgumentException.class, () -> stage.resume(pausedAt.minusNanos(1)));
        assertPaused(stage, pausedAt);
    }

    @Test
    void shouldAllowZeroLengthPause() {
        var stage = new StudySessionStage(START);
        stage.pause(START);
        stage.resume(START);
        assertOngoing(stage);
    }

    @Test
    void shouldRejectNullStartTime() {
        assertThrows(IllegalArgumentException.class, () -> new StudySessionStage(null));
    }

    private void assertPaused(StudySessionStage stage, Instant pausedAt) {
        assertEquals(StudySessionStageStatus.PAUSED, stage.getStatus());
        assertEquals(pausedAt, stage.getPausedAt());
        assertEquals(Duration.ZERO, stage.getAccumulatedPause());
        assertEquals(START, stage.getStartedAt());
    }

    private void assertOngoing(StudySessionStage stage) {
        assertEquals(StudySessionStageStatus.ONGOING, stage.getStatus());
        assertNull(stage.getPausedAt());
        assertEquals(Duration.ZERO, stage.getAccumulatedPause());
        assertEquals(START, stage.getStartedAt());
    }
}
