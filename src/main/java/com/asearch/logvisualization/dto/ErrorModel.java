package com.asearch.logvisualization.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ErrorModel {

    private int code;
    private String message;

    public ErrorModel(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
