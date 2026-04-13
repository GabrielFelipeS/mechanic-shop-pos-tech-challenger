package org.project.mechanic_shop.common.errors;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.project.mechanic_shop.common.responses.ApiResponse;
import org.project.mechanic_shop.common.responses.ErrorResponse;
import org.project.mechanic_shop.exception.ApiException;
import org.project.mechanic_shop.exception.DuplicatedRegisterException;
import org.project.mechanic_shop.exception.OperationNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getFieldErrors();
        List<ErrorField> listErrors = fieldErrors.stream()
                .map(fe -> new ErrorField(fe.getField(), fe.getDefaultMessage()))
                .toList();
        return new ErrorResponse(
                HttpStatus.UNPROCESSABLE_CONTENT.value(), "Validation error.", listErrors);
    }



    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntimeException(RuntimeException e) {
        log.error(" ############################################### Error: {}", String.valueOf(e.getCause()));
        log.error(" ############################################### Error: {}", e.getMessage());
        e.printStackTrace();
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Unexpected error: Contact the technical team.",
                List.of());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleThrowable(Throwable e) {
        log.error(" ####################################" +
                "CRITICAL ERROR: " + e.getMessage());
        e.printStackTrace();
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Critical error internal. contact the technical team.",
                List.of()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleAuthenticationException(AuthenticationException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "Authentication required.";
        return ErrorResponse.defaultResponse("Unauthorized: " + msg);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAccessDeniedException(AccessDeniedException e) {
        return ErrorResponse.forbidden("Access denied.");
    }


    @ExceptionHandler(DuplicatedRegisterException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicatedResgisterException(DuplicatedRegisterException e){
        String message = e.getMessage() != null ? e.getMessage() : "Already exists a register with same data.";
        return ErrorResponse.conflict(message);

    }

    @ExceptionHandler(OperationNotPermitted.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleOperationNotPermitted(OperationNotPermitted e) {
        return ErrorResponse.defaultResponse(e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body(new ErrorResponse(413, "The file exceeds the maximum allowed size of 5MB.", List.of()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException() {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Failed to process the input file or data.",
                List.of()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoSuchElementException( NoSuchElementException exception){
        return ErrorResponse.notFound("Object not found");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {


        if (ex.getCause() instanceof InvalidFormatException ifx && ifx.getTargetType() != null && ifx.getTargetType().isEnum()) {
            String sentValue = ifx.getValue().toString();
            String message = String.format("The value '%s' is not accepted for this field.", sentValue);
            return ErrorResponse.defaultResponse(message);
        }

        log.error("JSON error detected: {}", String.valueOf(ex.getCause()));

        return ErrorResponse.defaultResponse("Malformed JSON request or invalid data type.");
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse> handleApiException(ApiException e){
        ApiResponse body = new ApiResponse(e.getStatus().value(), e.getMessage(), List.of());
        return ResponseEntity.status(e.getStatus()).body(body);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        if(message != null && message.contains("ORA-00001")) {
            return ErrorResponse.conflict("Database constraint violation: Duplicate entry detected.");
        }

        return ErrorResponse.conflict("Database data integrity violation.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException e) {

        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String message = e.getReason() != null ? e.getReason() : status.getReasonPhrase();

        ErrorResponse body = new ErrorResponse(
                status.value(),
                message,
                List.of()
        );

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleEntityNotFoundException(EntityNotFoundException e) {
        return ErrorResponse.notFound(e.getMessage());
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();

        if (message != null && message.contains("No enum constant")) {
            String value = message.substring(message.lastIndexOf('.') + 1);
            String friendlyMessage = String.format("The value '%s' is not accepted for this field.", value);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.defaultResponse(friendlyMessage));
        }

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.conflict(message));
    }


    @ExceptionHandler(org.springframework.web.reactive.function.client.WebClientResponseException.Conflict.class)
    public ResponseEntity<ErrorResponse> handleKeycloakConflict(org.springframework.web.reactive.function.client.WebClientResponseException.Conflict ex) {

        log.warn("Attempting to create a duplicate resource in Keycloak: {}", ex.getMessage());

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "This username or email address is already registered in the system.",
                java.util.Collections.emptyList()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials() {

        ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid email or password", List.of() );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }


}
