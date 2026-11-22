package lv.gderuki.studytracker.exception;

public class ActiveSessionAlreadyExistsException extends RuntimeException {

    public ActiveSessionAlreadyExistsException(String message) {
        super(message);
    }

}
