package com.asearch.logvisualization.dto;

import lombok.Data;

@Data
public class LogCountByMinutesModel {
	private long logCount;
	private String startTime;
	private String endTime;
}
