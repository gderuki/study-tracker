package lv.gderuki.studytracker.exception;

public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException(String message) {
        super(message);
    }

}
