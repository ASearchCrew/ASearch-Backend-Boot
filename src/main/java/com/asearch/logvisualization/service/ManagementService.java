package com.asearch.logvisualization.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.asearch.logvisualization.dto.LogCountBySecondsModel;
import com.asearch.logvisualization.dto.RegisterServerDto;

public interface ManagementService {

    void modifyFilebeatConf(String path) throws Exception;


    void registerServerToMonitor(RegisterServerDto serverInfo) throws IOException;


    List<HashMap<String, Object>> getLogCountList() throws Exception;


	List<HashMap<String, Object>> getDateCountList() throws Exception;


	List<Object> getServerList() throws IOException;


	List<HashMap<String, Object>> getKeywordCountList(String hostName) throws IOException;


	void deleteServerToMonitor(String hostIp)  throws IOException;
	

	LogCountBySecondsModel getLogCountBySeconds() throws IOException;

}
