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
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
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
    private RestHighLevelClient client;
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
                    if (updateResponse.status() != RestStatus.OK) {
                        log.info("우영 실패");
                        throw new InternalServerErrorException("DB Malfunction");
                    }
                    else {
                        log.info("우영 성공");
                        return 3;
                    }
//                    else throw new AlreadyExistsException("Success");//FIXME 이쪽에서 종료되게 해야하는데 어떻게 할지.?
//                    else return "";
                });
        if (String.valueOf(3).equals(res.toString())) {
            log.info("우영 성공2");
            return;
        }
        log.info("우영 성공3");
        log.info(res.toString());
        log.info(document.get("keywords").toString());
//        if(res.getClass().toString().equals("String")) return;

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
    @Override
    public void detectKeyword() throws IOException {
        log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        log.info("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        SearchHit[] searchHits = alarmDao.getKeywordList(
                buildSearchRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, null),
                buildSearchSourceRequest(), "all", 1000);

        log.info(Arrays.toString(searchHits));
        log.info("===============1스케쥴 시작=======================");
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<KeywordModel>>(){}.getType();
        Stream<SearchHit> hitStream = Arrays.stream(searchHits);
        hitStream.forEach(server -> { // serverList
            if (server.getSourceAsMap().get("keywords") != null) {
                log.info("=========새로운 관리 서버 시작=============");
                //TODO - java8
                List<KeywordModel> keywords = gson.fromJson(server.getSourceAsMap().get("keywords").toString(), type);
                List<String> strings = new ArrayList<>();

                //todo Parallel
                if (keywords.size() > 0) { // keywords 배열
//                    keywords.forEach(keyword -> {//searchHit.getId()+"*"
                    int keywordPosition = 0;
                    for(KeywordModel keyword : keywords) {
                        log.info("keyword :: {}", keyword.getKeyword());
//                        SearchRequest searchRequest = buildSearchRequest("filebeat*", null, null);
                        SearchRequest searchRequest = buildSearchRequest(server.getId()+"*", null, null);
                        String[] includeFields = new String[] {"@timestamp", "message"};
                        String[] excludeFields = new String[] {};
                        SearchSourceBuilder searchSourceBuilder = buildSearchSourceRequest();
                        searchSourceBuilder.fetchSource(includeFields, excludeFields);

                        searchSourceBuilder.query(QueryBuilders.boolQuery()
                                        .must(QueryBuilders.termQuery("message", keyword.getKeyword())));
//                        searchSourceBuilder.query(QueryBuilders.termQuery("message", keyword.getKeyword()));
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
//                        searchSourceBuilder.size(10);
                        searchRequest.source(searchSourceBuilder);

                        try {
                            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT); // keyword response Data
                            log.info(response.status().toString());
                            log.info(response.getHits().getTotalHits() + " ~~");
                            /** =================여기 까지 Request 끝=====================*/

                            if (response.getHits().getTotalHits() != 0) {
                                Type timeType = new TypeToken<ArrayList<OccurrenceTimeDto>>(){}.getType();
                                List<OccurrenceTimeDto> occurrenceTimeList = gson.fromJson(server.getSourceAsMap().get("keywords").toString(), timeType);
                                if (occurrenceTimeList.get(keywordPosition).getLastOccurrenceTime() == null) {
                                    log.info("@@@@@@@@@@@@@@@ 첫 First Push 발송 @@@@@@@@@@@@@@@@@@@@@@");
                                    log.info("@@@@@@@@@@@@@@@ 첫 First Push 발송 @@@@@@@@@@@@@@@@@@@@@@");
                                    log.info("@@@@@@@@@@@@@@@ 첫 First Push 발송 @@@@@@@@@@@@@@@@@@@@@@");
                                    //TODO Push Service -- Topic 으로 바꾸자.
                                    //TODO Get Token
                                    SearchRequest searchRequestToken = buildSearchRequest(TOKEN_SERVER_INDEX, TOKEN_SERVER_TYPE, null);
                                    SearchSourceBuilder searchSourceBuilderToken = buildSearchSourceRequest();
                                    searchSourceBuilderToken.query(QueryBuilders.matchAllQuery());
                                    searchRequestToken.source(searchSourceBuilderToken);
                                    SearchResponse searchResponse = client.search(searchRequestToken, RequestOptions.DEFAULT);

                                    searchResponse.getHits().forEach(item -> {
//                                        item.getSourceAsMap().get("token").toString();
                                        JSONObject body = new JSONObject();
                                        body.put("to", item.getSourceAsMap().get("token").toString());
                                        body.put("priority", "high");
                                        JSONObject notification = new JSONObject();
                                        notification.put("title", "Log = ");
                                        notification.put("body", keyword.getKeyword());
                                        JSONObject data = new JSONObject();
                                        data.put("Key-1", "JSA Data 1");
                                        data.put("Key-2", "JSA Data 2");

                                        body.put("notification", notification);
                                        body.put("data", data);

                                        HttpEntity<String> request = new HttpEntity<>(body.toString());

                                        CompletableFuture<String> pushNotification = webPushNotificationsService.send(request);
                                        CompletableFuture.allOf(pushNotification).join();
                                    });


//                                    JSONObject body = new JSONObject();
//                                    body.put("to", "dSyujRt4psg:APA91bGZvmTH7sZ1Hz40EsAgndSedbZMxaPBdZlmE0C3ryPnVCe_WpHjr5F8N5d1UnRxpKu7gyh5_qYGHO0eX_Apqbmmld7xIfMjjhkcF3-fX-kWyMqolyHNUmgAsrJRT4T9Z0dV4omH");
//                                    body.put("priority", "high");
//                                    JSONObject notification = new JSONObject();
//                                    notification.put("title", "Log = ");
//                                    notification.put("body", keyword.getKeyword());
//                                    JSONObject data = new JSONObject();
//                                    data.put("Key-1", "JSA Data 1");
//                                    data.put("Key-2", "JSA Data 2");
//
//                                    body.put("notification", notification);
//                                    body.put("data", data);
//
//                                    HttpEntity<String> request = new HttpEntity<>(body.toString());
//
//                                    CompletableFuture<String> pushNotification = webPushNotificationsService.send(request);
//                                    CompletableFuture.allOf(pushNotification).join();

                                    //TODO last_occurrence_time 등록.
                                    Calendar calendar = Calendar.getInstance();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    Date date = dateFormat.parse(response.getHits().getHits()[0].getSourceAsMap().get("@timestamp").toString());
                                    calendar.setTime(date);
                                    calendar.getTimeInMillis();
//                                    log.info(calendar.getTimeInMillis() + " ~"); // 잘됨.

                                    UpdateRequest updateRequest = buildUpdateRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, server.getId());
                                    Map<String, Object> parameters = new HashMap<>();
                                    Map<String, Object> myObject = new HashMap<>();
                                    myObject.put("keyword", keyword.getKeyword());
                                    myObject.put("lastOccurrenceTime", String.valueOf(calendar.getTimeInMillis()));
                                    parameters.put("keyword", myObject);
                                    //TODO script 로 lastOccurrenceTime == null 조건문을 줄일 수 있다. 리팩토링 해야 한다.
                                    String idOrCode = "ctx._source.keywords["+keywordPosition+"] = params.keyword";
//                                    log.info(idOrCode);

                                    Script inline = new Script(ScriptType.INLINE, "painless",
                                            idOrCode, parameters);
                                    updateRequest.script(inline);
                                    UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
                                    //TODO Update 성공확인 할 것
                                } else {
                                    log.info("3333333333");
                                    log.info(occurrenceTimeList.get(keywordPosition).getLastOccurrenceTime());


                                    Calendar calendar = Calendar.getInstance();
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                    Date date = dateFormat.parse(response.getHits().getHits()[0].getSourceAsMap().get("@timestamp").toString());
                                    calendar.setTime(date);
                                    calendar.getTimeInMillis();

                                    // @timestamp - lastOccurrenceTime
                                    long diff = calendar.getTimeInMillis()
                                            - Long.parseLong(occurrenceTimeList.get(keywordPosition).getLastOccurrenceTime());
                                    log.info(String.valueOf(calendar.getTimeInMillis()));
                                    log.info(String.valueOf(diff));



                                    if (calendar.getTimeInMillis() > Long.parseLong(occurrenceTimeList.get(keywordPosition).getLastOccurrenceTime())) {
                                        //@timestamp > last_occurrence_time
                                        log.info("COME IN");
                                        if (diff > Long.parseLong(server.getSourceAsMap().get("interval").toString())) {
                                            log.info("@@@@@@@@@@@@@@@ 두번 이상 Push 발송 @@@@@@@@@@@@@@@@@@@@@@");
                                            log.info("@@@@@@@@@@@@@@@ 두번 이상 Push 발송 @@@@@@@@@@@@@@@@@@@@@@");
                                            log.info("@@@@@@@@@@@@@@@ 두번 이상 Push 발송 @@@@@@@@@@@@@@@@@@@@@@");
                                            //TODO Push Service
                                            //TODO Get Token
                                            SearchRequest searchRequestToken = buildSearchRequest(TOKEN_SERVER_INDEX, TOKEN_SERVER_TYPE, null);
                                            SearchSourceBuilder searchSourceBuilderToken = buildSearchSourceRequest();
                                            searchSourceBuilderToken.query(QueryBuilders.matchAllQuery());
                                            searchRequestToken.source(searchSourceBuilderToken);
                                            SearchResponse searchResponse = client.search(searchRequestToken, RequestOptions.DEFAULT);

                                            searchResponse.getHits().forEach(item -> {
//                                        item.getSourceAsMap().get("token").toString();
                                                JSONObject body = new JSONObject();
                                                body.put("to", item.getSourceAsMap().get("token").toString());
                                                body.put("priority", "high");
                                                JSONObject notification = new JSONObject();
                                                notification.put("title", "Log = ");
                                                notification.put("body", keyword.getKeyword());
                                                JSONObject data = new JSONObject();
                                                data.put("Key-1", "JSA Data 1");
                                                data.put("Key-2", "JSA Data 2");

                                                body.put("notification", notification);
                                                body.put("data", data);

                                                HttpEntity<String> request = new HttpEntity<>(body.toString());

                                                CompletableFuture<String> pushNotification = webPushNotificationsService.send(request);
                                                CompletableFuture.allOf(pushNotification).join();
                                            });

//                                            JSONObject body = new JSONObject();
//                                            body.put("to", "dSyujRt4psg:APA91bGZvmTH7sZ1Hz40EsAgndSedbZMxaPBdZlmE0C3ryPnVCe_WpHjr5F8N5d1UnRxpKu7gyh5_qYGHO0eX_Apqbmmld7xIfMjjhkcF3-fX-kWyMqolyHNUmgAsrJRT4T9Z0dV4omH");
//                                            body.put("priority", "high");
//                                            JSONObject notification = new JSONObject();
//                                            notification.put("title", "Log = ");
//                                            notification.put("body", keyword.getKeyword());
//                                            JSONObject data = new JSONObject();
//                                            data.put("Key-1", "JSA Data 1");
//                                            data.put("Key-2", "JSA Data 2");
//
//                                            body.put("notification", notification);
//                                            body.put("data", data);
//
//                                            HttpEntity<String> request = new HttpEntity<>(body.toString());
//
//                                            CompletableFuture<String> pushNotification = webPushNotificationsService.send(request);
//                                            CompletableFuture.allOf(pushNotification).join();

                                            //TODO last_occurrence_time 업데이트.
                                            Calendar calendar1 = Calendar.getInstance();
                                            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                            Date date1 = dateFormat1.parse(response.getHits().getHits()[0].getSourceAsMap().get("@timestamp").toString());
                                            calendar1.setTime(date1);
                                            calendar1.getTimeInMillis();

                                            UpdateRequest updateRequest = buildUpdateRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, server.getId());
                                            Map<String, Object> parameters = new HashMap<>();
                                            Map<String, Object> myObject = new HashMap<>();
                                            myObject.put("keyword", keyword.getKeyword());
                                            myObject.put("lastOccurrenceTime", String.valueOf(calendar.getTimeInMillis()));
//                                            myObject.put("lastOccurrenceTime", "AAAAA");
                                            parameters.put("keyword", myObject);
                                            //TODO script 로 lastOccurrenceTime == null 조건문을 줄일 수 있다. 리팩토링 해야 한다.
                                            String idOrCode = "ctx._source.keywords["+keywordPosition+"] = params.keyword";
                                            log.info(idOrCode);

                                            Script inline = new Script(ScriptType.INLINE, "painless",
                                                    idOrCode, parameters);
                                            updateRequest.script(inline);
                                            UpdateResponse updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
                                        } else if (diff < Long.parseLong(server.getSourceAsMap().get("interval").toString())) {
                                            // 그냥 넘어 간다.
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


/**
 * {
 *   "_index" : "filebeat-6.5.0-2018.11.22",
 *   "_type" : "doc",
 *   "_id" : "Gq-XOmcBOxmke417TvSE",
 *   "_score" : 9.424807,
 *   "_source" : {
 *     "@timestamp" : "2018-11-22T08:44:13.439Z",
 *     "message" : "컴퓨터 개고수"
 *   }
 * }
 */


                /**
                 * resource 를 적게 사용하는법을 생각해 보자.
                 */
//                if (keywords.size() > 0) {
//
//                    keywords.forEach(x -> {
//                        strings.add(x.getKeyword()); // list x 안에 객체가 없으면 안들어와진다.
////                    log.info("키워드 = " + x.getKeyword());
//                    });
//                    log.info(strings.toString());
//
//                    /**
//                     * Call Logic
//                     */
//                    SearchRequest searchRequest = buildSearchRequest("_all", null, null);
//                    String[] includeFields = new String[] {"@timestamp", "message"};
//                    String[] excludeFields = new String[] {};
//                    SearchSourceBuilder searchSourceBuilder = buildSearchSourceRequest();
//                    searchSourceBuilder.fetchSource(includeFields, excludeFields);
//                    searchSourceBuilder.query(QueryBuilders.termsQuery("message", strings));
//                    searchRequest.source(searchSourceBuilder);
//                    try {
//                        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
//                        log.info(response.status().toString());
//                        log.info(response.getHits().getTotalHits() + " ~~");
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//
//                }


            }
        });
        log.info("==============================1스케쥴 끝==================================");
    }
}
