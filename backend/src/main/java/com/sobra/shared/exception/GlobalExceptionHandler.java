package com.sobra.shared.exception;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> handleApiException(ApiException exception) {
        LOGGER.warn("Request rejected with {}: {}", exception.getCode(), exception.getMessage());
        return response(exception.getStatus(), exception.getCode(), exception.getMessage(), exception.getDetails());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        List<ErrorDetail> details = exception.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(error -> new ErrorDetail(error.getField(), error.getDefaultMessage()))
                .toList();
        return response(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                "La petición contiene campos inválidos.", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception) {
        List<ErrorDetail> details = exception.getConstraintViolations().stream()
                .map(violation -> new ErrorDetail(violation.getPropertyPath().toString(), violation.getMessage()))
                .sorted(Comparator.comparing(ErrorDetail::field))
                .toList();
        return response(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                "La petición contiene parámetros inválidos.", details);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiError> handleMethodValidation() {
        return response(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                "La petición contiene parámetros inválidos.",
                List.of(new ErrorDetail("request", "Revisa los parámetros enviados.")));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiError> handleMissingParameter(MissingServletRequestParameterException exception) {
        return response(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                "Falta un parámetro obligatorio.",
                List.of(new ErrorDetail(exception.getParameterName(), "es obligatorio")));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        return response(HttpStatus.BAD_REQUEST, ErrorCode.MALFORMED_REQUEST,
                "Un parámetro tiene formato inválido.",
                List.of(new ErrorDetail(exception.getName(), "formato inválido")));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadableBody() {
        return response(HttpStatus.BAD_REQUEST, ErrorCode.MALFORMED_REQUEST,
                "El cuerpo JSON es inválido o contiene un valor no admitido.",
                List.of(new ErrorDetail("request", "JSON inválido")));
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, OptimisticLockException.class})
    ResponseEntity<ApiError> handleOptimisticLock(Exception exception) {
        LOGGER.warn("Optimistic locking conflict", exception);
        return response(HttpStatus.CONFLICT, ErrorCode.STALE_VERSION,
                "El alimento fue modificado por otra operación.", List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> handleNoResource() {
        return response(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND,
                "El recurso solicitado no existe.", List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception exception) {
        LOGGER.error("Unexpected request failure", exception);
        return response(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
                "Ocurrió un error inesperado.", List.of());
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status,
            ErrorCode code,
            String message,
            List<ErrorDetail> details
    ) {
        return ResponseEntity.status(status).body(new ApiError(code.name(), message, details));
    }
}
