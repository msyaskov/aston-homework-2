package aston.h2.service;

public class CuratorNotFoundException extends RuntimeException {

    public CuratorNotFoundException(String message) {
        super(message);
    }

    public CuratorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
