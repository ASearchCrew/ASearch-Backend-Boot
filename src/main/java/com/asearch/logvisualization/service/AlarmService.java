package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dto.AlarmKeywordDto;
import com.asearch.logvisualization.dto.KeywordListModel;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


public interface AlarmService {

    void registerAlarmKeyword(AlarmKeywordDto keywordInfo) throws IOException;

    void removeKeyword(AlarmKeywordDto keyword) throws IOException;

    List<KeywordListModel> getKeywordList() throws IOException;

    void detectKeyword() throws IOException;
}
