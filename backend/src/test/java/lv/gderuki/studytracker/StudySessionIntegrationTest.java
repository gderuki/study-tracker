package lv.gderuki.studytracker;

import com.jayway.jsonpath.JsonPath;
import lv.gderuki.studytracker.entity.SessionStatus;
import lv.gderuki.studytracker.repository.StudySessionRepository;
import lv.gderuki.studytracker.repository.StudyTopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureRestTestClient
public class StudySessionIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:19.0");

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private StudyTopicRepository studyTopicRepository;

    @Autowired
    private StudySessionRepository studySessionRepository;

    @BeforeEach
    public void setup() {
        studySessionRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void shouldStartSessionForExistingTopic() {
        var topic = studyTopicRepository.findAll().stream()
                .filter(t -> t.getIsNiceToHave().equals(false))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No topic found!"));

        var response = restTestClient.post()
                .uri("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("topicId", topic.getId()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.status").isEqualTo("STARTED")
                .jsonPath("$.startTime").isNotEmpty()
                .jsonPath("$.endTime").isEmpty()
                .jsonPath("$.studyTopic.id").isEqualTo(topic.getId())
                .jsonPath("$.studyTopic.title").isEqualTo(topic.getTitle())
                .jsonPath("$.studyTopic.description").isEqualTo(topic.getDescription())
                .jsonPath("$.studyTopic.category").isEqualTo(topic.getCategory())
                .jsonPath("$.studyTopic.niceToHave").isEqualTo(topic.getIsNiceToHave())
                .returnResult();

        var body = response.getResponseBody();
        assertNotNull(body);
        Number sessionId = JsonPath.read(new String(body, StandardCharsets.UTF_8), "$.id");

        var savedSession = studySessionRepository.findById(sessionId.longValue())
                .orElseThrow(() -> new AssertionError("You shouldn't get there!"));

        assertEquals(1L, studySessionRepository.count());
        assertEquals(topic.getId(), savedSession.getTopic().getId());
        assertEquals(SessionStatus.STARTED, savedSession.getStatus());
        assertNotNull(savedSession.getStartTime());
        assertNull(savedSession.getEndTime());
    }

    @Test
    void shouldReturnNotFoundForMissingTopic() {
        final long nonExistentTopicId = -1L;

        assertFalse(studyTopicRepository.existsById(nonExistentTopicId));
        restTestClient.post()
                .uri("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("topicId", nonExistentTopicId))
                .exchange()
                .expectStatus().isNotFound();

        assertEquals(0L, studySessionRepository.count());
    }

    @Test
    void shouldRejectStartingSecondActiveSession() {
        var topic = studyTopicRepository.findAll().stream()
                .filter(t -> t.getIsNiceToHave().equals(false))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No topic found!"));

        var firstResponse = restTestClient.post()
                .uri("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("topicId", topic.getId()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        restTestClient.post()
                .uri("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("topicId", topic.getId()))
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);


        assertEquals(1L, studySessionRepository.count());
        var body = firstResponse.getResponseBody();
        assertNotNull(body);
        Number sessionId = JsonPath.read(new String(body, StandardCharsets.UTF_8), "$.id");

        var savedSession = studySessionRepository.findById(sessionId.longValue())
                .orElseThrow(() -> new AssertionError("You shouldn't get there!"));

        assertEquals(topic.getId(), savedSession.getTopic().getId());
        assertEquals(SessionStatus.STARTED, savedSession.getStatus());
        assertNotNull(savedSession.getStartTime());
        assertNull(savedSession.getEndTime());
    }

    @Test
    void shouldReturnActiveSession() {
        var topic = studyTopicRepository.findAll().stream()
                .filter(t -> t.getIsNiceToHave().equals(false))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No topic found!"));

        var response = restTestClient.post()
                .uri("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("topicId", topic.getId()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        var body = response.getResponseBody();
        assertNotNull(body);
        Number sessionId = JsonPath.read(new String(body, StandardCharsets.UTF_8), "$.id");

        restTestClient.get()
                .uri("/api/sessions/active")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("STARTED")
                .jsonPath("$.studyTopic.id").isEqualTo(topic.getId())
                .jsonPath("$.id").isEqualTo(sessionId.longValue());
    }

    @Test
    void shouldReturnNoContentWhenNoActiveSession() {
        var topic = studyTopicRepository.findAll().stream()
                .filter(t -> t.getIsNiceToHave().equals(false))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No topic found!"));

        restTestClient.get()
                .uri("/api/sessions/active")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void shouldAllowOnlyOneSessionWhenStartedConcurrently() throws Exception {
        var topic = studyTopicRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new AssertionError("No topic found!"));

        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);

        Callable<Integer> startSession = () -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting to start requests");
            }

            return restTestClient.post()
                    .uri("/api/sessions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("topicId", topic.getId()))
                    .exchange()
                    .expectBody()
                    .returnResult()
                    .getStatus().value();
        };

        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(startSession);
            var second = executor.submit(startSession);

            try {
                assertTrue(ready.await(10, TimeUnit.SECONDS), "Request tasks did not become ready");
                start.countDown();

                var statuses = List.of(
                        first.get(30, TimeUnit.SECONDS),
                        second.get(30, TimeUnit.SECONDS));
                assertEquals(List.of(201, 409), statuses.stream().sorted().toList());
            } finally {
                start.countDown();
                first.cancel(true);
                second.cancel(true);
                executor.shutdownNow();
            }
        }

        assertEquals(1L, studySessionRepository.count());
        var activeSession = studySessionRepository.findStudySessionByStatus(SessionStatus.STARTED)
                .orElseThrow(() -> new AssertionError("No active session after concurrent requests"));
        assertEquals(topic.getId(), activeSession.getTopic().getId());
        assertNotNull(activeSession.getStartTime());
        assertNull(activeSession.getEndTime());
    }

    @Test
    void shouldCompleteStartedSession() {
        var topic = studyTopicRepository.findAll().stream()
                .filter(t -> t.getIsNiceToHave().equals(false))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No topic found!"));

        var response = restTestClient.post()
                .uri("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("topicId", topic.getId()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        var body = response.getResponseBody();
        assertNotNull(body);
        Number sessionId = JsonPath.read(new String(body, StandardCharsets.UTF_8), "$.id");

        restTestClient.post()
                .uri("/api/sessions/{sessionId}/complete", sessionId.longValue())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(sessionId.longValue())
                .jsonPath("$.status").isEqualTo("COMPLETED")
                .jsonPath("$.endTime").isNotEmpty();

        var savedSession = studySessionRepository.findById(sessionId.longValue())
                .orElseThrow(() -> new AssertionError("Completed session not found"));

        assertEquals(SessionStatus.COMPLETED, savedSession.getStatus());
        assertNotNull(savedSession.getEndTime());
        assertFalse(savedSession.getEndTime().isBefore(savedSession.getStartTime()));
        assertEquals(1L, studySessionRepository.count());
        assertFalse(studySessionRepository.existsByStatus(SessionStatus.STARTED));
    }

    @Test
    void shouldReturnNotFoundWhenCompletingMissingSession() {
        var topic = studyTopicRepository.findAll().stream()
                .filter(t -> t.getIsNiceToHave().equals(false))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No topic found!"));

        var response = restTestClient.post()
                .uri("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("topicId", topic.getId()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        var body = response.getResponseBody();
        assertNotNull(body);
        Number sessionId = JsonPath.read(new String(body, StandardCharsets.UTF_8), "$.id");

        long nonExistentSessionId = -1L;
        assertFalse(studySessionRepository.existsById(nonExistentSessionId));
        restTestClient.post()
                .uri("/api/sessions/{sessionId}/complete", nonExistentSessionId)
                .exchange()
                .expectStatus().isNotFound();

        var savedSession = studySessionRepository.findById(sessionId.longValue())
                .orElseThrow(() -> new AssertionError("Original session not found"));

        assertEquals(SessionStatus.STARTED, savedSession.getStatus());
        assertNull(savedSession.getEndTime());
        assertEquals(1L, studySessionRepository.count());
    }

    @Test
    void shouldRejectCompletingAlreadyCompletedSession() {
        var topic = studyTopicRepository.findAll().stream()
                .filter(t -> t.getIsNiceToHave().equals(false))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No topic found!"));

        var response = restTestClient.post()
                .uri("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("topicId", topic.getId()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        var body = response.getResponseBody();
        assertNotNull(body);
        Number sessionId = JsonPath.read(new String(body, StandardCharsets.UTF_8), "$.id");

        restTestClient.post()
                .uri("/api/sessions/{sessionId}/complete", sessionId.longValue())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(sessionId.longValue())
                .jsonPath("$.status").isEqualTo("COMPLETED")
                .jsonPath("$.endTime").isNotEmpty();

        var savedSession = studySessionRepository.findById(sessionId.longValue())
                .orElseThrow(() -> new AssertionError("Completed session not found"));

        assertEquals(SessionStatus.COMPLETED, savedSession.getStatus());

        var originalEndTime = savedSession.getEndTime();
        assertNotNull(originalEndTime);

        restTestClient.post()
                .uri("/api/sessions/{sessionId}/complete", sessionId.longValue())
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);

        var sessionAfterRepeatedCompletion = studySessionRepository.findById(sessionId.longValue())
                .orElseThrow(() -> new AssertionError("Completed session not found"));

        assertEquals(originalEndTime, sessionAfterRepeatedCompletion.getEndTime());
        assertEquals(SessionStatus.COMPLETED, sessionAfterRepeatedCompletion.getStatus());
        assertEquals(1L, studySessionRepository.count());

        assertFalse(studySessionRepository.existsByStatus(SessionStatus.STARTED));
    }

    @Test
    void shouldAllowStartingNewSessionAfterCompletion() {
        var topic = studyTopicRepository.findAll().stream()
                .filter(t -> t.getIsNiceToHave().equals(false))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No topic found!"));

        var firstResponse = restTestClient.post()
                .uri("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("topicId", topic.getId()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        var firstResponseBody = firstResponse.getResponseBody();
        assertNotNull(firstResponseBody);
        Number firstSessionId = JsonPath.read(new String(firstResponseBody, StandardCharsets.UTF_8), "$.id");

        restTestClient.post()
                .uri("/api/sessions/{sessionId}/complete", firstSessionId.longValue())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(firstSessionId.longValue())
                .jsonPath("$.status").isEqualTo("COMPLETED")
                .jsonPath("$.endTime").isNotEmpty();

        var firstSession = studySessionRepository.findById(firstSessionId.longValue())
                .orElseThrow(() -> new AssertionError("Completed session not found"));

        assertEquals(SessionStatus.COMPLETED, firstSession.getStatus());

        var secondResponse = restTestClient.post()
                .uri("/api/sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("topicId", topic.getId()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .returnResult();

        var secondResponseBody = secondResponse.getResponseBody();
        assertNotNull(secondResponseBody);
        Number secondSessionId = JsonPath.read(new String(secondResponseBody, StandardCharsets.UTF_8), "$.id");

        var secondSession = studySessionRepository.findById(secondSessionId.longValue())
                .orElseThrow(() -> new AssertionError("New active session not found"));

        var firstSessionAfterNewStart = studySessionRepository.findById(firstSessionId.longValue())
                .orElseThrow(() -> new AssertionError("Original completed session not found"));

        assertNotEquals(firstSessionId.longValue(), secondSessionId.longValue());
        assertEquals(SessionStatus.COMPLETED, firstSessionAfterNewStart.getStatus());
        assertEquals(firstSession.getEndTime(), firstSessionAfterNewStart.getEndTime());
        assertNull(secondSession.getEndTime());
        assertEquals(topic.getId(), secondSession.getTopic().getId());
        assertEquals(SessionStatus.STARTED, secondSession.getStatus());
        assertEquals(2L, studySessionRepository.count());
    }

}
