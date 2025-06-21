package com.inghub.credit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BindingErrorsResponse {

    private List<BindingError> bindingErrors = new ArrayList<>();

    public void addAllErrors(BindingResult bindingResult) {
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            BindingError error = new BindingError();
            error.setObjectName(fieldError.getObjectName());
            error.setFieldName(fieldError.getField());
            error.setFieldValue(String.valueOf(fieldError.getRejectedValue()));
            error.setErrorMessage(fieldError.getDefaultMessage());
            addError(error);
        }
    }

    public void addError(BindingError bindingError) {
        this.bindingErrors.add(bindingError);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    protected static class BindingError {

        private String objectName;
        private String fieldName;
        private String fieldValue;
        private String errorMessage;
    }

}
