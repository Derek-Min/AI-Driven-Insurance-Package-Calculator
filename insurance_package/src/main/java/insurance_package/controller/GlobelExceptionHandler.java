package insurance_package.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobelExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<?> handleValidation(ValidationException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "ok", false,
                "error", e.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception e) {
        e.printStackTrace(); // IMPORTANT: shows stacktrace in console
        return ResponseEntity.internalServerError().body(Map.of(
                "ok", false,
                "error", e.getClass().getSimpleName(),
                "message", e.getMessage()
        ));
    }
}
