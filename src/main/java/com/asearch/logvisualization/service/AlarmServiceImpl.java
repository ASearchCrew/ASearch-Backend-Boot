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
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
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
    private RestHighLevelClient client;

    @Override
    public void registerAlarmKeyword(AlarmKeywordDto keywordInfo) throws IOException {
        Map<String, Object> parameters = new HashMap<>();
        Map<String, Object> document = Optional.ofNullable(alarmDao.getExistedKeywords(buildGetRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, keywordInfo.getHostName()),
                "keywords", keywordInfo.getKeyword()).getSourceAsMap())
                .orElseThrow(() -> new NotFoundException("Document 가 존재하지 않습니다."));
        Object res = Optional.ofNullable(document.get("keywords"))
                .orElseGet(() -> {
//                    Map<String, Object> parameters = new HashMap<>();
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
                    if (updateResponse.status() != RestStatus.OK) throw new InternalServerErrorException("DB Malfunction");
                    else throw new AlreadyExistsException("Success");//FIXME 이쪽에서 종료되게 해야하는데 어떻게 할지.?
//                    return updateResponse;
                });
        log.info(res.toString());
        log.info(document.get("keywords").toString());

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<KeywordDto>>(){}.getType();
        ArrayList<KeywordDto> arrayList = gson.fromJson(document.get("keywords").toString(), type);

        arrayList.forEach(x -> { //TODO java8
            if (keywordInfo.getKeyword().equals(x.getKeyword())) throw new AlreadyExistsException("Existed Keyword");
        });
        Map<String, Object> myObject = new HashMap<>();
        myObject.put("keyword", keywordInfo.getKeyword());
        parameters.put("keyword", myObject);
        UpdateResponse updateResponse = alarmDao.addKeyword(parameters, buildUpdateRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, keywordInfo.getHostName()));
        if (updateResponse.status() != RestStatus.OK) throw new InternalServerErrorException("DB Malfunction");

    }

    //TODO 3순위
    @Override
    public void removeKeyword(AlarmKeywordDto keyword) throws IOException {

        SearchHit[] searchHits = alarmDao.getKeywordList(buildSearchRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, null),
                buildSearchSourceRequest(),
                "all",
                1000);

        /**
         * 1. response 개수 만큼 포문을 돌린다.
         * 2. 각 for 문 (array 안에 있는 keyword에서 2중 for문을 돌려서 찾아야 한다.) 에서는
         */


        Stream<SearchHit> hitStream = Arrays.stream(searchHits);
        hitStream.forEach(x -> {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<KeywordModel>>(){}.getType();
            ArrayList<KeywordModel> arrayList = gson.fromJson(x.getSourceAsMap().get("keywords").toString(), type);
            Map<String, Object> parameters = new HashMap<>();
            for (int i = 0; i < arrayList.size(); i++) {
                if (arrayList.get(i).getKeyword().equals(keyword.getKeyword())) {
//                    DeleteRequest deleteRequest = buildDeleteRequest(KEYWORD_INDEX, KEYWORD_TYPE, documentId);
                    try {
                        alarmDao.removeKeyword(parameters, buildUpdateRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, keyword.getHostName()), i);
//                        DeleteResponse deleteResponse = alarmDao.removeKeywordDocument(buildDeleteRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, keyword.getHostName()));
                        //TODO 삭제 성공 확인 하기
                        throw new NotFoundException("AAAAA");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            arrayList.forEach(y -> {

            });
        });

//        DeleteRequest deleteRequest
//                buildDeleteRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, keyword.getHostName());



//        String documentId = null;
//
//        SearchRequest searchRequest = buildSearchRequest(KEYWORD_INDEX, KEYWORD_TYPE, null);
//        SearchHit[] searchHits = alarmDao.getExistedKeyword(searchRequest, buildSearchSourceRequest(), new String[]{"keyword", "host_ip"},
//                new String[]{keyword.getKeyword(), keyword.getHostIp()});
//
//        for (SearchHit searchHit : searchHits)
//            if (searchHit.getSourceAsMap().get("keyword").toString().equals(keyword.getKeyword()))
//                documentId = searchHit.getId();
//        if (documentId == null)
//            throw new NotFoundException("No Data");
//        else {
//            DeleteRequest deleteRequest = buildDeleteRequest(KEYWORD_INDEX, KEYWORD_TYPE, documentId);
//            DeleteResponse deleteResponse = alarmDao.removeKeywordDocument(deleteRequest);
////            //TODO 삭제 확인 할 것
////        }
    }

    @Override
    public List<KeywordListModel> getKeywordList() throws IOException {



//        alarmDao.getKeywords(buildSearchRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, null)
//                ,buildSearchSourceRequest()
//                ,"all", 1000);





//        SearchRequest searchRequest = buildSearchRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, null);
        SearchHit[] searchHits = alarmDao.getKeywordList(
                buildSearchRequest(MANAGEMENT_SERVER_INDEX, MANAGEMENT_SERVER_TYPE, null),
                buildSearchSourceRequest(),
                "all", 1000);
        List<KeywordListModel> keywordListModels = new ArrayList<>();
        Stream<SearchHit> hitStream = Arrays.stream(searchHits);
        hitStream.forEach(x -> {
            if (keywordListModels.size() == 0) {
                keywordListModels.add(new KeywordListModel(x.getId(), new ArrayList<>()));
                Gson gson = new Gson();
                Type type = new TypeToken<ArrayList<KeywordModel>>(){}.getType();
                ArrayList<KeywordModel> arrayList = gson.fromJson(x.getSourceAsMap().get("keywords").toString(), type);

                arrayList.forEach(y -> keywordListModels.get(0).getKeywords().add(new KeywordModel(y.getKeyword())));
            } else {
                keywordListModels.add(new KeywordListModel(x.getId(), new ArrayList<>()));
                Gson gson = new Gson();
//                JsonReader reader = new JsonReader(new StringReader(x.getSourceAsMap().get("keywords").toString()));
//                reader.setLenient(true);

//                ArrayList<KeywordModel> arrayList = gson.fromJson(x.getSourceAsMap().get("keywords").toString(), ArrayList.class);

                Type type = new TypeToken<ArrayList<KeywordModel>>(){}.getType();
//                String trimmed = x.getSourceAsMap().get("keywords").toString().trim();
                ArrayList<KeywordModel> arrayList = gson.fromJson(x.getSourceAsMap().get("keywords").toString(), type);

                arrayList.forEach(y -> keywordListModels.get(keywordListModels.size()-1).getKeywords().add(new KeywordModel(y.getKeyword())));

            }
        });
        return keywordListModels;
    }
}
