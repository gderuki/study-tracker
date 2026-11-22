package lv.gderuki.studytracker.controller.advice;

import lv.gderuki.studytracker.exception.ActiveSessionAlreadyExistsException;
import lv.gderuki.studytracker.exception.TopicNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(TopicNotFoundException.class)
    public ResponseEntity<String> handleTopicNotFound(
            TopicNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(ActiveSessionAlreadyExistsException.class)
    public ResponseEntity<String> handleActiveSessionAlreadyExistsException(
            ActiveSessionAlreadyExistsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }
}