package com.asearch.logvisualization.service;

import java.io.IOException;
import java.util.List;

public interface LogService {

    List<String> getRawLogs(int count) throws IOException;

    List<String> searchLog(String word);
}
