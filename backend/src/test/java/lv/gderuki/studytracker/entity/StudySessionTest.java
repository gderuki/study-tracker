package lv.gderuki.studytracker.entity;

import lv.gderuki.studytracker.exception.InvalidSessionStateException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class StudySessionTest {

    private static final Instant START_TIME = Instant.parse("2026-11-25T10:00:00Z");
    private static final Instant END_TIME = START_TIME.plusSeconds(60);

    @Test
    void shouldCompleteStartedSession() {
        var session = sessionWith(SessionStatus.STARTED, null);

        session.completeSession(END_TIME);

        assertEquals(SessionStatus.COMPLETED, session.getStatus());
        assertEquals(END_TIME, session.getEndTime());
        assertEquals(START_TIME, session.getStartTime());
    }

    @Test
    void shouldAllowCompletionAtStartTime() {
        var session = sessionWith(SessionStatus.STARTED, null);

        session.completeSession(START_TIME);

        assertEquals(SessionStatus.COMPLETED, session.getStatus());
        assertEquals(START_TIME, session.getEndTime());
    }

    @Test
    void shouldRejectCompletingCancelledSession() {
        var session = sessionWith(SessionStatus.CANCELLED, END_TIME);

        assertThrows(InvalidSessionStateException.class,
                () -> session.completeSession(END_TIME.plusSeconds(60)));

        assertUnchanged(session, SessionStatus.CANCELLED, END_TIME);
    }

    @Test
    void shouldRejectCompletingAlreadyCompletedSession() {
        var session = sessionWith(SessionStatus.COMPLETED, END_TIME);

        assertThrows(InvalidSessionStateException.class,
                () -> session.completeSession(END_TIME.plusSeconds(60)));

        assertUnchanged(session, SessionStatus.COMPLETED, END_TIME);
    }

    @Test
    void shouldRejectCompletionWithNullTime() {
        var session = sessionWith(SessionStatus.STARTED, null);

        assertThrows(IllegalArgumentException.class, () -> session.completeSession(null));

        assertUnchanged(session, SessionStatus.STARTED, null);
    }

    @Test
    void shouldRejectCompletionBeforeStartTime() {
        var session = sessionWith(SessionStatus.STARTED, null);

        assertThrows(IllegalArgumentException.class,
                () -> session.completeSession(START_TIME.minusNanos(1)));

        assertUnchanged(session, SessionStatus.STARTED, null);
    }

    private StudySession sessionWith(SessionStatus status, Instant endTime) {
        var topic = new StudyTopic(null, "Unit testing", "Domain invariants", "Implementation", false);
        return new StudySession(null, START_TIME, endTime, topic, status);
    }

    private void assertUnchanged(StudySession session, SessionStatus status, Instant endTime) {
        assertEquals(status, session.getStatus());
        assertEquals(endTime, session.getEndTime());
        assertEquals(START_TIME, session.getStartTime());
    }
}
