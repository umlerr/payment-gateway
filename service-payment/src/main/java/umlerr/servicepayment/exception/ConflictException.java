package umlerr.servicepayment.exception;

import java.io.Serial;

public class ConflictException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public ConflictException(String message) {
        super(message);
    }
}
