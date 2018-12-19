package com.asearch.logvisualization.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.OK)
public class SuccessRes extends BaseException {
    public SuccessRes(String message) {
        super(HttpStatus.OK.value(), message);
    }
}
