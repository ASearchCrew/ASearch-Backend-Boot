package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.LogModel;
import io.micrometer.core.lang.Nullable;

import java.io.IOException;
import java.util.List;

public interface LogService {

    List<LogModel> getRawLogs(String direction, String time, @Nullable String search) throws IOException;

    List<String> searchLog(String word) throws IOException;
}
