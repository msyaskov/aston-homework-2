package aston.hw2.service;

public class GroupNotFoundException extends RuntimeException {

    public GroupNotFoundException(String message) {
        super(message);
    }

    public GroupNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
