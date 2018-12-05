package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.AlarmKeywordDto;
import org.springframework.stereotype.Service;

import java.io.IOException;


public interface AlarmService {

    boolean registerAlarmKeyword(AlarmKeywordDto keyword) throws IOException;

    boolean removeKeyword(AlarmKeywordDto keyword) throws IOException;

    boolean getKeywordList();
}
