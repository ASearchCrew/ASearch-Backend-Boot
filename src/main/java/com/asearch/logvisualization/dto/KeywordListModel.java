package com.asearch.logvisualization.dto;

import io.micrometer.core.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class KeywordListModel {

    private String hostName;
    @Nullable private String interval;
    @Nullable private List<KeywordModel> keywords;

    public KeywordListModel(String hostName, String interval, List<KeywordModel> keywords) {
        this.hostName = hostName;
        this.interval = interval;
        this.keywords = keywords;
    }
}
