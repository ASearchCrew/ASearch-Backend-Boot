package com.asearch.logvisualization.dto;

import io.micrometer.core.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class KeywordListModel {

    private String hostName;
    @Nullable private List<KeywordModel> keywords;

    public KeywordListModel(String hostName, List<KeywordModel> keywords) {
        this.hostName = hostName;
        this.keywords = keywords;
    }
}
