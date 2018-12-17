package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.RegisterServerModel;
import com.asearch.logvisualization.dto.ServerListDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public interface ManagementService {

    void modifyFilebeatConf(String path) throws Exception;


    void registerServerToMonitor(RegisterServerModel serverInfo) throws IOException;


    List<HashMap<String, Object>> getLogCountList() throws Exception;


	List<HashMap<String, Object>> getDateCountList() throws Exception;


	List<Object> getServerList() throws IOException;


    List<ServerListDto> getServerListToMonitor() throws IOException;
}
