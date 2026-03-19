package top.elexvx.admin.exception;

public class ConcurrentLoginException extends RuntimeException {
    public ConcurrentLoginException(String message) {
        super(message);
    }
}
