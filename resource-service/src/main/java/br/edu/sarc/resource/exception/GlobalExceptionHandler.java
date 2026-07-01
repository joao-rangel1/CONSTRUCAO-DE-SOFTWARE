package br.edu.sarc.resource.exception;

import br.edu.sarc.common.dto.ErrorResponse;
import br.edu.sarc.common.exception.ApiExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(RecursoNotFoundException exception, HttpServletRequest request) {
        log.warn("Recurso nao encontrado: {} | path={}", exception.getMessage(), request.getRequestURI());
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }
}
