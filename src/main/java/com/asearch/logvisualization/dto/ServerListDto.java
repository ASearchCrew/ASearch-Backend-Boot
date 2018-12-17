package com.asearch.logvisualization.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@ToString
@Slf4j
@Builder
public class ServerListDto {

    private String hostIp;
    private String hostName;

    @Builder
    public ServerListDto(String hostIp, String hostName) {
        this.hostIp = hostIp;
        this.hostName = hostName;
    }
}
