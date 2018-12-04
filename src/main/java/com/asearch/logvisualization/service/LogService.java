package com.asearch.logvisualization.service;

import java.util.List;

public interface LogService {

    List<String> getRawLogs(int count);
}
