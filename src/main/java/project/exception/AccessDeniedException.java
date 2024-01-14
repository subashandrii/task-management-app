package project.exception;

public class AccessDeniedException extends CustomRuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
