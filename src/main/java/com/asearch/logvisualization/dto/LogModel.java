package com.asearch.logvisualization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LogModel {

    private String id;
    private String timeStamp;
    private String message;
}
