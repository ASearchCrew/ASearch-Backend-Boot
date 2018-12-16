package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.LogInfoDto;
import io.micrometer.core.lang.Nullable;

import java.io.IOException;
import java.text.ParseException;

public interface LogService {

    LogInfoDto getRawLogs(String direction, String hostName, String time, @Nullable String search,
                          boolean isStream, long initialCount, long upScrollOffset) throws IOException, ParseException;


    String getDocument(String id) throws IOException;
}
