package com.asearch.logvisualization.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@ToString
@Slf4j
public class RegisterServerModel {

    private String hostIp;
    private String hostName;

    @Builder
    public RegisterServerModel(String hostIp, String hostName) {
        this.hostIp = hostIp;
        this.hostName = hostName;
    }
}
