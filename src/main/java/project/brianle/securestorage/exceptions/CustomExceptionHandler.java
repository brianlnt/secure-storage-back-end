package project.brianle.securestorage.exceptions;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import project.brianle.securestorage.domain.Response;
import project.brianle.securestorage.utils.RequestUtils;

import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class CustomExceptionHandler extends ResponseEntityExceptionHandler implements ErrorController {
    private final HttpServletRequest request;

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest webRequest) {
        log.error(String.format("handleExceptionInternal: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, statusCode), statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode statusCode, WebRequest webRequest) {
        log.error(String.format("handleMethodArgumentNotValid: %s", exception.getMessage()));
        var fieldErrors = exception.getBindingResult().getFieldErrors();
        var fieldErrorsMessage = fieldErrors.stream().map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(fieldErrorsMessage, ExceptionUtils.getRootCauseMessage(exception), request, statusCode), statusCode);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Response> handleCustomException(CustomException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Response> badCredentialsException(BadCredentialsException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<Response> sQLIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ResponseEntity<Response> unrecognizedPropertyException(UnrecognizedPropertyException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Response> accessDeniedException(AccessDeniedException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Response> exception(Exception exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Response> transactionSystemException(TransactionSystemException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<Response> emptyResultDataAccessException(EmptyResultDataAccessException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(CredentialsExpiredException.class)
    public ResponseEntity<Response> credentialsExpiredException(CredentialsExpiredException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<Response> disabledException(DisabledException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<Response> lockedException(LockedException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Response> duplicateKeyException(DuplicateKeyException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Response> dataIntegrityViolationException(DataIntegrityViolationException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Response> dataAccessException(DataAccessException exception) {
        log.error(String.format("CustomException: %s", exception.getMessage()));
        return new ResponseEntity<>(RequestUtils.handleErrorResponse(exception.getMessage(), ExceptionUtils.getRootCauseMessage(exception), request, BAD_REQUEST), BAD_REQUEST);
    }

    private String processErrorMessage(Exception exception){
        if(exception instanceof CustomException) { return exception.getMessage(); }
        if(exception.getMessage() != null){
            if(exception.getMessage().contains("duplicate") && exception.getMessage().contains("AccountVerification")){
                return "You already verified your account.";
            }
            if(exception.getMessage().contains("duplicate") && exception.getMessage().contains("ResetPasswordVerifications")){
                return "We already sent you an email to reset your password.";
            }
            if(exception.getMessage().contains("duplicate") && exception.getMessage().contains("Key (email)")){
                return "Email already exists. Use a different email and try again.";
            }
            if(exception.getMessage().contains("duplicate")){
                return "Duplicate entry. Please try again.";
            }
        }
        return "An error occurred. Please try again.";
    }
}
