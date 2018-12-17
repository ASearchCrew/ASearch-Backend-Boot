package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dao.AlarmDaoImpl;
import com.asearch.logvisualization.dto.AlarmKeywordDto;
import com.asearch.logvisualization.dto.KeywordDto;
import com.asearch.logvisualization.dto.KeywordListModel;
import com.asearch.logvisualization.dto.KeywordModel;
import com.asearch.logvisualization.exception.AlreadyExistsException;
import com.asearch.logvisualization.exception.InternalServerErrorException;
import com.asearch.logvisualization.exception.NotFoundException;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Stream;

import static com.asearch.logvisualization.util.Constant.*;

@AllArgsConstructor
@Service
@Slf4j
public class AlarmServiceImpl extends BaseServiceImpl implements AlarmService {

    private AlarmDaoImpl alarmDao;

    @Override
    public void registerAlarmKeyword(AlarmKeywordDto keywordInfo) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        Map<String, Object> document = Optional.ofNullable(alarmDao.getExistedKeywords(buildGetRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, keywordInfo.getHostName()),
                "keywords", keywordInfo.getKeyword()).getSourceAsMap())
                .orElseThrow(() -> new NotFoundException("Document 가 존재하지 않습니다."));
        Object res = Optional.ofNullable(document.get("keywords"))
                .orElseGet(() -> {
                    parameters.put("keywords", new ArrayList<>());
                    UpdateResponse updateResponse = null;
                    try {
                        updateResponse = alarmDao.makeNewKeywords(parameters, buildUpdateRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, keywordInfo.getHostName()));
                        parameters.clear();
                        Map<String, Object> myObject = new HashMap<>();
                        myObject.put("keyword", keywordInfo.getKeyword());
                        parameters.put("keyword", myObject);
                        updateResponse = alarmDao.addKeyword(parameters, buildUpdateRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, keywordInfo.getHostName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        parameters.clear();
                    }
                    assert updateResponse != null;
                    if (updateResponse.status() != RestStatus.OK)
                        throw new InternalServerErrorException("DB Malfunction");
                    else throw new AlreadyExistsException("Success");//FIXME 이쪽에서 종료되게 해야하는데 어떻게 할지.?
//                    else return "";
                });
        log.info(res.toString());
        log.info(document.get("keywords").toString());
//        if(res.getClass().toString().equals("String")) return;

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<KeywordDto>>() {
        }.getType();
        ArrayList<KeywordDto> arrayList = gson.fromJson(document.get("keywords").toString(), type);

        arrayList.forEach(x -> {
            if (keywordInfo.getKeyword().equals(x.getKeyword())) throw new AlreadyExistsException("Existed Keyword");
        });
        Map<String, Object> myObject = new HashMap<>();
        myObject.put("keyword", keywordInfo.getKeyword());
        parameters.put("keyword", myObject);
        UpdateResponse updateResponse = alarmDao.addKeyword(parameters, buildUpdateRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, keywordInfo.getHostName()));
        if (updateResponse.status() != RestStatus.OK) throw new InternalServerErrorException("DB Malfunction");

    }

    @Override
    public void removeKeyword(AlarmKeywordDto keyword) throws IOException {

        SearchHit[] searchHits = alarmDao.getKeywordList(buildSearchRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, null),
                buildSearchSourceRequest(),
                "all",
                1000);
        Stream<SearchHit> hitStream = Arrays.stream(searchHits);
        hitStream.forEach(x -> {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<KeywordModel>>() {
            }.getType();
            ArrayList<KeywordModel> arrayList = gson.fromJson(x.getSourceAsMap().get("keywords").toString(), type);
            Map<String, Object> parameters = new HashMap<>();
            for (int i = 0; i < arrayList.size(); i++) {
                if (arrayList.get(i).getKeyword().equals(keyword.getKeyword())) {
                    try {
                        alarmDao.removeKeyword(parameters, buildUpdateRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, keyword.getHostName()), i);
                        //TODO 삭제 성공 확인 하기
                        throw new NotFoundException("AAAAA");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //TODO 글자 띄어쓰기 있을때, converter 오류 수정 하기.
    @Override
    public List<KeywordListModel> getKeywordList() throws IOException {

        SearchHit[] searchHits = alarmDao.getKeywordList(
                buildSearchRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, null),
                buildSearchSourceRequest(),
                "all", 1000);
        List<KeywordListModel> keywordListModels = new ArrayList<>();
        Stream<SearchHit> hitStream = Arrays.stream(searchHits);
        Gson gson = new Gson();
        hitStream.forEach(searchHit -> {
            keywordListModels.add(new KeywordListModel(searchHit.getId(), new ArrayList<>()));
            Type type = new TypeToken<ArrayList<KeywordModel>>(){}.getType();
            if (searchHit.getSourceAsMap().get("keywords") != null) {
                List<KeywordModel> keywords = gson.fromJson(searchHit.getSourceAsMap().get("keywords").toString(), type);
                int position = keywordListModels.size() == 0 ? 0 : keywordListModels.size() - 1;
                keywords.forEach(keyword -> Objects
                        .requireNonNull(keywordListModels.get(position).getKeywords())
                        .add(new KeywordModel(keyword.getKeyword())));
            }
        });
        return keywordListModels;
    }

    @Override
    public void detectKeyword() throws IOException {

        SearchHit[] searchHits = alarmDao.getKeywordList(
                buildSearchRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, null),
                buildSearchSourceRequest(), "all", 1000);

//        log.info(Arrays.toString(searchHits));
        /**
         * 1. 위 는 hostName 별 keywords 를 조회 해 왔다.
         * //TODO 2. hostName 별(1중 for 문) keywords 에서 keyword 별로(2중 for 문 or termquery 에 여러 내용을 포함하여 쿼리를 날리는걸로) 발생했는지 콜을 하여 검사를 한다.
         * //TODO 2.1 발생 한 시간을 last_occurrence_time 에 등록 한다.
         * //TODO 2.2 해당 keyword log 가 last_occurrence_time 보다 최근 시간에 발생 했다면, last_occurrence_time 을 update 하고, 알람을 또 보낸다.
         * 위의 2 ~ 2.2 까지는 keyword log 발생시 push 를 1번만 보내는 logic 이다.
         * 아래는 3 ~ 는 keyword log 발생시 push 를 해당 로그가 발생 하지 않을 때까지 주기적으로 push 를 보내는 logic 이다. //
         *  발생이 언제하고 언제 안할지 모르고 확정적이지 않으므로, keyword log 재발생 시간의 시간적 제약(2분 or 3분 interval)을
         *  두어서 제약 시간 이내에 로그 발생시 다시 push 를 전송 하는 logic 으로 만들어 본다.
         *    -- 그렇다면 last_occurrence_time 과 로그 제약시간의 차를 구하여, 제약시간 보다 적다면 push 를 다시 보내는데,
         *      --> 생각보니까 그냥 발생할때마다 보내면 되려나?
         *  발생한 로그들의 갯수를 구해서 count 를 추가 해 보자.
         *
         *  ====
         *  마지막 발생 시간과, interval 시간 차이의 시간에는 발생하면 시간을 보내지 않는다. 마지막 발생 시간과, + 1시간 (interval 이 필요 없는..?)
         */


    }
}
