package com.asearch.logvisualization.dto;

import lombok.Data;

@Data
public class LogCountBySecondsModel {
	private long logCount;
	private String startTime;
	private String endTime;
}
