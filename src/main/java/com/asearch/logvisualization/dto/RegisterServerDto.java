package com.asearch.logvisualization.dto;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Data
@ToString
@Slf4j
public class RegisterServerDto {

    private String hostIp;
    private String hostName;
}