package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.LogModel;
import io.micrometer.core.lang.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface LogService {

    List<LogModel> getRawLogs(String direction, String time, @Nullable String search, boolean isStream) throws IOException, ParseException;


    String getDocument(String id) throws IOException;
}
