package nbu.edomoupravitel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class LogicOperationException extends RuntimeException {
    public LogicOperationException(String message) {
        super(message);
    }

    public LogicOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
