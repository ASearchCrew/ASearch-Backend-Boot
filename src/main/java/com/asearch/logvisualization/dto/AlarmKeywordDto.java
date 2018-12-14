package com.asearch.logvisualization.dto;

import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

@Data
@ToString
@Slf4j
public class AlarmKeywordDto {

    private String keyword;
    private String hostName;
}
