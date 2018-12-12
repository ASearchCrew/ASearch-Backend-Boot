package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.RegisterServerDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.springframework.http.ResponseEntity;

public interface ManagementService {

    //void modifyFilebeatConf(String path) throws Exception;


    void registerServerToMonitor(RegisterServerDto serverInfo) throws IOException;


    List<HashMap<String, Object>> getLogCountList() throws Exception;


	List<HashMap<String, Object>> getDateCountList() throws Exception;


	List<Object> getServerList() throws IOException;


	void modifyFilebeatConf(String path) throws Exception;
}
