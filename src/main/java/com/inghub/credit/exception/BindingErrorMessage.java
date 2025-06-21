package com.inghub.credit.exception;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"timestamp", "statusCode", "message", "errors", "path"})
public class BindingErrorMessage extends ErrorResponse {

    private List<BindingErrorsResponse.BindingError> errors;

    public BindingErrorMessage(Date timestamp, int statusCode, String message, String path, List<BindingErrorsResponse.BindingError> errors) {
        super(timestamp, statusCode, message, path);
        this.errors = errors;
    }
}
