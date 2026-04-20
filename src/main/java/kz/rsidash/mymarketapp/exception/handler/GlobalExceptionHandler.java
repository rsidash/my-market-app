package kz.rsidash.mymarketapp.exception.handler;

import kz.rsidash.mymarketapp.exception.NotFoundException;
import kz.rsidash.mymarketapp.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> handleNotFound(NotFoundException ex) {
        log.debug("[NotFoundException] {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto(
                        String.valueOf(HttpStatus.NOT_FOUND.value()),
                        ErrorCode.NOT_FOUND
                ));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorDto> handleValidation(ValidationException ex) {
        log.debug("[ValidationException] {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorDto(
                        String.valueOf(HttpStatus.BAD_REQUEST.value()),
                        ErrorCode.BAD_REQUEST
                ));
    }
}
