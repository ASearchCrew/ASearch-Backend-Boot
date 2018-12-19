package com.asearch.logvisualization.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class LogCountBySecondsModel {
	private List<LogCountByMinutesModel> charDatas = new ArrayList<LogCountByMinutesModel>();
}
