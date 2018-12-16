package com.asearch.logvisualization.dto;

import lombok.Data;

import java.util.List;

@Data
public class LogInfoDto {

    private long sumCount;
    private List<LogModel> logs;
}
