package com.asearch.logvisualization.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class AlreadyExistsException extends BaseException {
    public AlreadyExistsException(int statusCode, String message) {
        super(statusCode, message);
    }
}
