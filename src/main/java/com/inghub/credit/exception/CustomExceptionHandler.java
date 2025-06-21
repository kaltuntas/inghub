package com.inghub.credit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> resourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse message = new ErrorResponse(new Date(),
                                                  HttpStatus.NOT_FOUND.value(),
                                                  ex.getMessage(),
                                                  ((ServletWebRequest) request).getRequest().getRequestURI());

        return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> globalExceptionHandler(Exception ex, WebRequest request) {
        ErrorResponse message = new ErrorResponse(new Date(),
                                                  HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                  ex.getMessage(),
                                                  ((ServletWebRequest) request).getRequest().getRequestURI());

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> bindingException(BindException ex, WebRequest request) {
        BindingErrorsResponse errors = new BindingErrorsResponse();
        errors.addAllErrors(ex.getBindingResult());

        BindingErrorMessage message = new BindingErrorMessage(new Date(),
                                                              HttpStatus.BAD_REQUEST.value(),
                                                              "Bad Request",
                                                              ((ServletWebRequest) request).getRequest().getRequestURI(),
                                                              errors.getBindingErrors());

        return new ResponseEntity<>(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
