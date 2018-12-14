package com.asearch.logvisualization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class KeywordListModel {

    private String hostName;
    private List<KeywordModel> keywords;

    public KeywordListModel(String hostName, List<KeywordModel> keywords) {
        this.hostName = hostName;
        this.keywords = keywords;
    }
}
