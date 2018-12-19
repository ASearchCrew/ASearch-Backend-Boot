package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.LogInfoDto;
import com.asearch.logvisualization.dto.PushTokenDto;
import io.micrometer.core.lang.Nullable;

import java.io.IOException;
import java.text.ParseException;

public interface LogService {

    LogInfoDto getRawLogs(String direction,
                          String hostName,
                          String time,
                          @Nullable String search,
                          boolean isStream,
                          long initialCount,
                          long upScrollOffset,
                          @Nullable String id,
                          @Nullable String calendarStartTime,
                          @Nullable String calendarEndTime) throws IOException, ParseException;


    String getDocument(String id) throws IOException;

    void registerPushToken(PushTokenDto dto) throws IOException;
}
