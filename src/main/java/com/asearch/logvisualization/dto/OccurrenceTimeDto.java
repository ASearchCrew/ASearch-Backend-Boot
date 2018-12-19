package com.asearch.logvisualization.dto;

import lombok.Data;
import lombok.ToString;

@Data
public class OccurrenceTimeDto {

//    private String keyword;
    private String lastOccurrenceTime;

    public OccurrenceTimeDto(String lastOccurrenceTime) {
//        this.keyword = keyword;
        this.lastOccurrenceTime = lastOccurrenceTime;
    }
}
