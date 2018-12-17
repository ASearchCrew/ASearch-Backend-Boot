package com.asearch.logvisualization.dto;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Data
@ToString
@Slf4j
public class RegisterServerModel {

    private String hostIp;
    private String hostName;
}
