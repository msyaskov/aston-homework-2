package aston.hw2.util;

public class RequiredPathVariableNotFoundException extends RuntimeException {

    public RequiredPathVariableNotFoundException(String message) {
        super(message);
    }

    public RequiredPathVariableNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
