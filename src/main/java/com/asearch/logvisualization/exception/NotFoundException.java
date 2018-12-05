package com.asearch.logvisualization.exception;

import org.apache.logging.log4j.spi.NoOpThreadContextMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND.value(), message);
    }
}
