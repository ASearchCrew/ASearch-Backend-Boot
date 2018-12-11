package com.asearch.logvisualization.exception;

import com.asearch.logvisualization.dto.ErrorModel;
import javafx.util.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ASearchExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public @ResponseBody
    ResponseEntity<ErrorModel> handleException(BaseException e) {
        ErrorModel error = new ErrorModel(e.getStatusCode(), e.getMessage());
        switch (e.getStatusCode()) {
            case 400:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            case 404:
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            case 409:
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            case 500:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
            default:
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorModel(0, "unknownError"));
        }
    }
}
