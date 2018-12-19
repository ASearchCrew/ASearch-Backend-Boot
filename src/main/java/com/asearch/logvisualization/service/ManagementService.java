package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.RegisterServerModel;
import com.asearch.logvisualization.dto.ServerListDto;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.asearch.logvisualization.dto.LogCountBySecondsModel;

public interface ManagementService {

    void modifyFilebeatConf(String path) throws Exception;


    void registerServerToMonitor(RegisterServerModel serverInfo) throws IOException;


    List<HashMap<String, Object>> getLogCountList() throws Exception;


	List<HashMap<String, Object>> getDateCountList() throws Exception;


	List<Object> getServerList() throws IOException;


//<<<<<<< HEAD
//    List<ServerListDto> getServerListToMonitor() throws IOException;
//=======
	List<HashMap<String, Object>> getKeywordCountList(String hostName) throws IOException;


	void deleteServerToMonitor(String hostIp)  throws IOException;
	

	LogCountBySecondsModel getLogCountBySeconds() throws IOException;
	
//>>>>>>> is-11-configure
}
