package kz.rsidash.mymarketapp.exception.handler;

import io.micrometer.common.util.StringUtils;
import kz.rsidash.mymarketapp.exception.NotFoundException;
import kz.rsidash.mymarketapp.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(value = {NotFoundException.class})
    protected ResponseEntity<Object> handleNotFoundException(
            final NotFoundException ex,
            final WebRequest webRequest
    ) {
        if (StringUtils.isNotBlank(ex.getMessage())) {
            log.debug("[NotFoundException] message - [{}]", ex.getMessage());
        }

        return handleExceptionInternal(ex,
                new ErrorDto(Integer.toString(HttpStatus.NOT_FOUND.value()), ErrorCode.NOT_FOUND),
                new HttpHeaders(), HttpStatus.NOT_FOUND, webRequest
        );
    }

    @ExceptionHandler(value = {ValidationException.class})
    protected ResponseEntity<Object> handleValidationException(
            final ValidationException ex,
            final WebRequest webRequest
    ) {
        if (StringUtils.isNotBlank(ex.getMessage())) {
            log.debug("[ValidationException] message - [{}]", ex.getMessage());
        }

        return handleExceptionInternal(ex,
                new ErrorDto(Integer.toString(HttpStatus.BAD_REQUEST.value()), ErrorCode.BAD_REQUEST),
                new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest
        );
    }
}
