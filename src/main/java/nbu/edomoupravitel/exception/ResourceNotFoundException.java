package nbu.edomoupravitel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// грешка, която се хвърля, когато търсен запис не съществува в базата данни
// @ResponseStatus автоматично връща Error 404 (Not Found), ако грешката не бъде прихваната
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
