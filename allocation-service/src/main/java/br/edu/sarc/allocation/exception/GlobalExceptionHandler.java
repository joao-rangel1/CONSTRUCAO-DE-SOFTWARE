package br.edu.sarc.allocation.exception;

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

    @ExceptionHandler(AlocacaoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(AlocacaoNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException exception, HttpServletRequest request) {
        log.warn("Erro de negocio: {} | path={}", exception.getMessage(), request.getRequestURI());
        return build(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenOperationException exception, HttpServletRequest request) {
        log.warn("Acesso negado: {} | path={}", exception.getMessage(), request.getRequestURI());
        return build(HttpStatus.FORBIDDEN, exception.getMessage(), request, Map.of());
    }
}
