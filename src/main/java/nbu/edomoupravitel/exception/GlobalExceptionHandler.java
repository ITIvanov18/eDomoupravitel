package nbu.edomoupravitel.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;


// вместо да се пишат try-catch конструкции във всеки метод,
// логиката за обработка на грешки стои тук, защото чрез @ControllerAdvice
// класът слуша за грешки във всички контролери на приложението

@ControllerAdvice
public class GlobalExceptionHandler {

    // прихваща случаите, когато търсен обект липсва в базата данни
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    // прихваща нарушения на бизнес логиката
    @ExceptionHandler(LogicOperationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleLogicOperationException(LogicOperationException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    // safety net за всички останали неочаквани грешки
    // NullPointerException, Database Connection Fail и тн.
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGlobalException(Exception ex, Model model) {
        model.addAttribute("errorMessage", "An unexpected error occurred: " + ex.getMessage());
        return "error";
    }
}
