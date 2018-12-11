package com.asearch.logvisualization.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class KeywordListModel {

    private String hostIp;
    private List<KeywordModel> keywords;

    public KeywordListModel(String hostIp, List<KeywordModel> keywords) {
        this.hostIp = hostIp;
        this.keywords = keywords;
    }
}
