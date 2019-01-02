package com.asearch.logvisualization.service;

import com.asearch.logvisualization.dao.AlarmDaoImpl;
import com.asearch.logvisualization.dto.*;
import com.asearch.logvisualization.exception.AlreadyExistsException;
import com.asearch.logvisualization.exception.InternalServerErrorException;
import com.asearch.logvisualization.exception.NotFoundException;
import com.asearch.logvisualization.push.WebPushNotificationService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static com.asearch.logvisualization.util.Constant.*;

@AllArgsConstructor
@Service
@Slf4j
public class AlarmServiceImpl extends BaseServiceImpl implements AlarmService {

    private AlarmDaoImpl alarmDao;
    private WebPushNotificationService webPushNotificationsService;

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
                    else
                        return 3;
                });
        if (String.valueOf(3).equals(res.toString()))
            return;

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<KeywordDto>>() {}.getType();
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
            Type type = new TypeToken<ArrayList<KeywordModel>>(){}.getType();
            if (x.getSourceAsMap().get("keywords") != null) {
                ArrayList<KeywordModel> arrayList = gson.fromJson(x.getSourceAsMap().get("keywords").toString(), type);
                Map<String, Object> parameters = new HashMap<>();
                for (int i = 0; i < arrayList.size(); i++) {
                    if (arrayList.get(i).getKeyword().equals(keyword.getKeyword())) {
                        try {
                            alarmDao.removeKeyword(parameters, buildUpdateRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, keyword.getHostName()), i);
                            //TODO 삭제 성공 확인 하기
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
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
            if (searchHit.getSourceAsMap().get("interval") != null)
                keywordListModels.add(new KeywordListModel(searchHit.getId(), searchHit.getSourceAsMap().get("interval").toString(), new ArrayList<>()));
            else
                keywordListModels.add(new KeywordListModel(searchHit.getId(), "none", new ArrayList<>()));
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

    //TODO 시간복잡도 를 줄이려면 어떻게 해야..?
    //TODO last_occurrence_time 을 추가해보는 것도 좋은데 mapping 이나 여러 고려사항이 있을거 같다.
    //FIXME 알람이 한번 울린 키워드 가 등록될시 알람이 울린다.
    /**
     * //Fixme
     * resource 를 적게 사용하는법을 생각해 보자.
     */
    @Override
    public void detectKeyword() throws IOException {
        SearchHit[] searchHits = alarmDao.getKeywordList(
                buildSearchRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, null),
                buildSearchSourceRequest(), "all", 1000);

        log.info("==================Start=======================");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<KeywordModel>>(){}.getType();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        Stream<SearchHit> hitStream = Arrays.stream(searchHits);
        hitStream.forEach(server -> { // serverList
            if (server.getSourceAsMap().get("keywords") != null) {
                log.info("=========새로운 관리 서버 시작=============");

                Date date; //Todo Atomic 은 forEach는 해당, for(:) 는 비해당 Because..
                List<KeywordModel> keywords = gson.fromJson(server.getSourceAsMap().get("keywords").toString(), type);
                //todo Parallel
                if (keywords.size() > 0) { // keywords 배열
                    int keywordPosition = 0;
                    for (KeywordModel keyword : keywords) {
                        try {
                            SearchResponse response = alarmDao.findByMessageLog(buildSearchRequest(
                                    server.getId()+"*", null, null),
                                    buildSearchSourceRequest(),
                                    keyword.getKeyword());

                            if (response.getHits().getTotalHits() != 0) {
                                Type timeType = new TypeToken<ArrayList<OccurrenceTimeDto>>(){}.getType();
                                List<OccurrenceTimeDto> occurrenceTimeList = gson.fromJson(server.getSourceAsMap().get("keywords").toString(), timeType);
                                if (occurrenceTimeList.get(keywordPosition).getLastOccurrenceTime() == null) {
                                    //Todo Duplicate Code
                                    date = dateFormat.parse(response.getHits().getHits()[0].getSourceAsMap().get("@timestamp").toString());
                                    calendar.setTime(date);
                                    UpdateResponse updateResponse = alarmDao.updateKeyword(
                                            buildUpdateRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, server.getId()),
                                            makeParameters(keyword.getKeyword(), String.valueOf(calendar.getTimeInMillis())),
                                            keywordPosition);
                                    //TODO Update 성공확인 할 것
                                } else {
                                    log.info(occurrenceTimeList.get(keywordPosition).getLastOccurrenceTime());
                                    date = dateFormat.parse(response.getHits().getHits()[0].getSourceAsMap().get("@timestamp").toString());
                                    calendar.setTime(date);

                                    // @timestamp - lastOccurrenceTime
                                    long diff = calendar.getTimeInMillis()
                                            - Long.parseLong(occurrenceTimeList.get(keywordPosition).getLastOccurrenceTime());
                                    log.info(String.valueOf(calendar.getTimeInMillis()));

                                    if (calendar.getTimeInMillis() > Long.parseLong(occurrenceTimeList.get(keywordPosition).getLastOccurrenceTime())) {
                                        //@timestamp > last_occurrence_time
                                        if (diff > Long.parseLong(server.getSourceAsMap().get("interval").toString())) {
                                            //Get Token
                                            SearchResponse searchResponse = alarmDao.getTokenList(
                                                    buildSearchRequest(TOKEN_SERVER_INDEX, TOKEN_SERVER_TYPE, null),
                                                    buildSearchSourceRequest());
                                            //Push Service -- Topic 으로 바꾸자.
                                            searchResponse.getHits().forEach(item -> sendPushNoti(item.getSourceAsMap().get("token").toString(), keyword.getKeyword()));
                                            date = dateFormat.parse(response.getHits().getHits()[0].getSourceAsMap().get("@timestamp").toString());
                                            calendar.setTime(date);
                                            UpdateResponse updateResponse = alarmDao.updateKeyword(
                                                    buildUpdateRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, server.getId()),
                                                    makeParameters(keyword.getKeyword(), String.valueOf(calendar.getTimeInMillis())),
                                                    keywordPosition);
                                        } else if (diff < Long.parseLong(server.getSourceAsMap().get("interval").toString())) {
                                            date = dateFormat.parse(response.getHits().getHits()[0].getSourceAsMap().get("@timestamp").toString());
                                            calendar.setTime(date);
                                            UpdateResponse updateResponse = alarmDao.updateKeyword(
                                                    buildUpdateRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, server.getId()),
                                                    makeParameters(keyword.getKeyword(), String.valueOf(calendar.getTimeInMillis())),
                                                    keywordPosition);
                                        } else {
                                            //TODO Exception
                                        }
                                    } else {
                                        //@timestamp <= last_occurrence_time
                                        log.info("TEST");
                                    }
                                }
                            }
                        } catch (IOException | ParseException e) {
                            e.printStackTrace();
                        }
                        keywordPosition ++;
                    }
                }
            }
        });
        log.info("==============================Finish==================================");
    }


    private Map<String, Object> makeParameters(String keyword, String lastOccurrenceTime) {
        Map<String, Object> parameters = new HashMap<>();
        Map<String, Object> myObject = new HashMap<>();
        myObject.put("keyword", keyword);
        myObject.put("lastOccurrenceTime", lastOccurrenceTime);
        parameters.put("keyword", myObject);
        return parameters;
    }

    private void sendPushNoti(String token, String keyword) {
        JSONObject body = new JSONObject();
        body.put("to", token);
        body.put("priority", "high");
        JSONObject notification = new JSONObject();
        notification.put("title", "Log = ");
        log.info("푸시 보내기 전 키워드 확인" + keyword);
        notification.put("body", keyword); // FIXME push 에서 ??? 가 뜬다.
        JSONObject data = new JSONObject();
        data.put("Key-1", keyword);
        data.put("Key-2", "JSA Data 2");

        body.put("notification", notification);
        body.put("data", data);

        HttpEntity<String> request = new HttpEntity<>(body.toString());

        CompletableFuture<String> pushNotification = webPushNotificationsService.send(request);
        CompletableFuture.allOf(pushNotification).join();
    }
}


