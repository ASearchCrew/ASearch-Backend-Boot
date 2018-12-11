package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.RegisterServerDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.ResponseEntity;

public interface ManagementService {

    String modifyFilebeatConf();


    void registerServerToMonitor(RegisterServerDto serverInfo) throws IOException;


    List<HashMap<String, Object>> getLogCountList() throws Exception;
}
