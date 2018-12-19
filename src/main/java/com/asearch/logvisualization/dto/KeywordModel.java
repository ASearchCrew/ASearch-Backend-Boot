package com.asearch.logvisualization.dto;

import lombok.Data;

@Data
public class KeywordModel {

    private String keyword;
//    private String lastOccurrenceTime;

    public KeywordModel(String keyword) {
        this.keyword = keyword;
    }
}
