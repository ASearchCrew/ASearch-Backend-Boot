package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.RegisterServerDto;

import java.io.IOException;

public interface ManagementService {

    String modifyFilebeatConf();


    void registerServerToMonitor(RegisterServerDto serverInfo) throws IOException;
}
