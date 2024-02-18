package aston.hw2.service;

public class InvalidCandidateException extends RuntimeException {

    public InvalidCandidateException(String message) {
        super(message);
    }

    public InvalidCandidateException(String message, Throwable cause) {
        super(message, cause);
    }
}
