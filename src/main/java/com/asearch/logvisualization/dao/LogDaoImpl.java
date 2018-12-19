package com.asearch.logvisualization.dao;

import io.micrometer.core.lang.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Slf4j
@Repository
public class LogDaoImpl extends BaseDaoImpl implements LogDao {


    //TODO 1. 중복 코드 줄일 것 /  2. 아래 코드들이 Dao 에서 적절한가?
    @Override
    public SearchResponse getLogs(SearchRequest searchRequest,
                                  SearchSourceBuilder searchSourceBuilder,
                                  String direction,
                                  String hostName,
                                  String time,
                                  @Nullable String search,
                                  boolean isStream,
                                  long initialCount,
                                  long upScrollOffset,
                                  @Nullable String id,
                                  @Nullable String calendarStartTime,
                                  @Nullable String calendarEndTime) throws IOException {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        String[] includeFields = new String[] {"@timestamp", "input", "message"};
        String[] excludeFields = new String[] {};
        searchSourceBuilder.fetchSource(includeFields, excludeFields);

        //TODO searchTime 이 null 일 경우에 뭐가 들어오는지 check 하기.
        if (calendarEndTime != null) {
            if (search != null) {
                searchSourceBuilder.query(QueryBuilders.matchQuery("message", search));
                switch (direction) {
//                    case "up" :
//                        //TODO 1. 갯수가 500000개가 넘어갈시에는, 짤라서 계산해야 한다. 변수가 한개 필요 -- 500000개 미만일시 그냥 계산하기.
//                        /**
//                         * initialCount 는 : 0 <-- 이 안나오기 위해서 center Request 를 받았을 떄부터, localStarage 에 저장 해놓아야 한다.
//                         */
//                        log.info("up - search : {}", search);
//                        log.info("이전 마지막 time = {}" , Long.parseLong(time));
//                        log.info("id = {}", id);
//                        searchSourceBuilder.query(
//                                QueryBuilders.boolQuery()
//                                        .must(QueryBuilders.termQuery("message", search))
//                                        .filter(QueryBuilders.rangeQuery("@timestamp")
//                                                .from(String.valueOf(Long.parseLong(time) - 1000000000)) // 시간 Issue 해결 해야 한다.
//                                                .to(String.valueOf(Long.parseLong(time)))));
//                        Object[] objects = new Object[]{time, id};
//                        searchSourceBuilder.searchAfter(objects);
//                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
//                        searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
//                        //TODO 검색어가 있을 경우는 시간이 [현재시간-1000초(16분)] 보다 적은 데이터를 못보여주게 된다. -- EX 컴퓨터 개고수  -- 그러므로 시간을 걸면 안될듯?
////                        searchSourceBuilder.from(Integer.parseInt(String.valueOf(fromCount))); // --> offset --//FIXME 시간 없이 쓰면 too large 에러 난다.
//                        searchSourceBuilder.size(100);
//                        break;
//                    case "down" :
//                        log.info("down - search : {}", search);
//                        log.info("이전 최신 time = {}" , Long.parseLong(time));
//                        log.info("id = {}",id);
//                        searchSourceBuilder.query(
//                                QueryBuilders.boolQuery()
//                                        .must(QueryBuilders.termQuery("message", search))
//                                        .filter(QueryBuilders.rangeQuery("@timestamp")
//                                                .from(String.valueOf(Long.parseLong(time)))
//                                                .to(calendar.getTimeInMillis())));
//                        Object[] objectsa = new Object[]{time, id};
//                        searchSourceBuilder.searchAfter(objectsa);
//                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
//                        searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));
//                        searchSourceBuilder.size(100);
//                        break;
                    case "center" :
                        log.info("center 달력 - search : {}", search);
                        log.info("현재시간 = {} ", calendar.getTimeInMillis());
                        searchSourceBuilder.query(
                                QueryBuilders.boolQuery()
                                        .must(QueryBuilders.termQuery("message", search))
                                        .filter(QueryBuilders.rangeQuery("@timestamp")
//                                                .from(String.valueOf(Long.parseLong(time))) //Fixme center 인데 time 이 있다. 확인해보자.
                                                .from(calendarEndTime) //Fixme 해당 시간을 계산해서 하루가 안지나야 한다.
                                                .to(time)));
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                        searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
                        searchSourceBuilder.size(50);
                        break;
                    default:
                        break;
                }
            } else {
                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
                switch (direction) {
//                    case "up":
//                        log.info("검색어 없는 Up STREAM (위로 Scroll) 구역");
//                        log.info("이전 마지막 time = {}" , Long.parseLong(time));
//                        log.info("id = {}",id);
//                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
//                                .from(String.valueOf(Long.parseLong(time) - 100000)) //TODO 검색어가 없으면, 로그가 근방에 있으므로 마이너르슬 조금만 헀고,, 검색어가 있으면 로그가 근방이 아닌 멀리에 있을수도 있으므로 마이너스를 많이 했다.
//                                .to(String.valueOf(Long.parseLong(time)))); //100초
//                        Object[] objects = new Object[]{time, id};
//                        searchSourceBuilder.searchAfter(objects);
//                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
//                        searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
//                        searchSourceBuilder.size(100);
//                        break;
//                    case "down":
//                        log.info("검색어 없는 Down STREAM (아래로 STREAM) 구역");
//                        log.info("이전 최신 time = {}" , Long.parseLong(time));
//                        log.info("id = {}",id);
//                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
//                                .from(String.valueOf(Long.parseLong(time)))
//                                .to(calendar.getTimeInMillis()));
//                        Object[] objectsa = new Object[]{time, id};
//                        searchSourceBuilder.searchAfter(objectsa);
//                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
//                        searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));
//                        searchSourceBuilder.size(100);
//                        break;
                    case "center":
                        log.info("검색어 없는 Center 달력 구역");
                        log.info("현재시간 = {} ", calendar.getTimeInMillis());
                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
                                .from(calendarEndTime)
                                .to(time)); // -30초 ~ 현재 시간
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                        searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
                        searchSourceBuilder.size(50);
                        break;
                    default:
                        break;
                }
            }
        } else {
            /**
             *  LiveStream 을 켰을시.
             */
            if (isStream) {
                if (search != null) {
                    searchSourceBuilder.query(QueryBuilders.matchQuery("message", search));
                    switch (direction) {
                        case "up" :
                            //TODO 1. 갯수가 500000개가 넘어갈시에는, 짤라서 계산해야 한다. 변수가 한개 필요 -- 500000개 미만일시 그냥 계산하기.
                            /**
                             * initialCount 는 : 0 <-- 이 안나오기 위해서 center Request 를 받았을 떄부터, localStarage 에 저장 해놓아야 한다.
                             */
                            log.info("up STREAM - search : {}", search);
                            log.info("이전 마지막 time = {}" , Long.parseLong(time));
                            log.info("id = {}", id);
                            searchSourceBuilder.query(
                                    QueryBuilders.boolQuery()
                                            .must(QueryBuilders.termQuery("message", search))
                                            .filter(QueryBuilders.rangeQuery("@timestamp")
                                                    .from(String.valueOf(Long.parseLong(time) - 1000000000)) // 시간 Issue 해결 해야 한다.
                                                    .to(String.valueOf(Long.parseLong(time)))));
                            Object[] objects = new Object[]{time, id};
                            searchSourceBuilder.searchAfter(objects);
                            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
                            //TODO 검색어가 있을 경우는 시간이 [현재시간-1000초(16분)] 보다 적은 데이터를 못보여주게 된다. -- EX 컴퓨터 개고수  -- 그러므로 시간을 걸면 안될듯?
//                        searchSourceBuilder.from(Integer.parseInt(String.valueOf(fromCount))); // --> offset --//FIXME 시간 없이 쓰면 too large 에러 난다.
                            searchSourceBuilder.size(100);
                            break;
                        case "stream" :
                            log.info("down STREAM - search : {}", search);
                            log.info("이전 최신 time = {}" , Long.parseLong(time));
                            log.info("id = {}",id);
                            searchSourceBuilder.query(
                                    QueryBuilders.boolQuery()
                                            .must(QueryBuilders.termQuery("message", search))
                                            .filter(QueryBuilders.rangeQuery("@timestamp")
                                                    .from(String.valueOf(Long.parseLong(time)))
                                                    .to(calendar.getTimeInMillis())));
                            Object[] objectsa = new Object[]{time, id};
                            searchSourceBuilder.searchAfter(objectsa);
                            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
                            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));
                            searchSourceBuilder.size(100);
                            break;
                        case "center" :
                            log.info("center STREAM - search : {}", search);
                            log.info("현재시간 = {} ", calendar.getTimeInMillis());
                            searchSourceBuilder.query(
                                    QueryBuilders.boolQuery()
                                            .must(QueryBuilders.termQuery("message", search)));
                            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
                            searchSourceBuilder.size(100);
                            break;
                        default:
                            break;
                    }
                } else {
                    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
                    switch (direction) {
                        case "up":
                            log.info("검색어 없는 Up STREAM (위로 Scroll) 구역");
                            log.info("이전 마지막 time = {}" , Long.parseLong(time));
                            log.info("id = {}",id);
                            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
                                    .from(String.valueOf(Long.parseLong(time) - 100000))
                                    .to(String.valueOf(Long.parseLong(time)))); //100초
                            Object[] objects = new Object[]{time, id};
                            searchSourceBuilder.searchAfter(objects);
                            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
                            searchSourceBuilder.size(100);
                            break;
                        case "stream":
                            log.info("검색어 없는 Down STREAM (아래로 STREAM) 구역");
                            log.info("이전 최신 time = {}" , Long.parseLong(time));
                            log.info("id = {}",id);
                            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
                                    .from(String.valueOf(Long.parseLong(time)))
                                    .to(calendar.getTimeInMillis()));
                            Object[] objectsa = new Object[]{time, id};
                            searchSourceBuilder.searchAfter(objectsa);
                            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
                            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));
                            searchSourceBuilder.size(100);
                            break;
                        case "center":
                            log.info("검색어 없는 Center STREAM 구역");
                            log.info("현재시간 = {} ", calendar.getTimeInMillis());
                            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
//                                .from(String.valueOf(calendar.getTimeInMillis() - 30000))
                                    .to(calendar.getTimeInMillis())); // -30초 ~ 현재 시간
                            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
                            searchSourceBuilder.size(100);
                            break;
                        default:
                            break;
                    }
                }


            } else {
                /**
                 *  검색어(Search) 가 있을떄
                 */
                if (search != null) {
                    searchSourceBuilder.query(QueryBuilders.matchQuery("message", search));
                    switch (direction) {
                        case "up" :
                            //TODO 1. 갯수가 500000개가 넘어갈시에는, 짤라서 계산해야 한다. 변수가 한개 필요 -- 500000개 미만일시 그냥 계산하기.
                            /**
                             * initialCount 는 : 0 <-- 이 안나오기 위해서 center Request 를 받았을 떄부터, localStarage 에 저장 해놓아야 한다.
                             */
                            log.info("up - search : {}", search);
                            log.info("이전 마지막 time = {}" , Long.parseLong(time));
                            log.info("id = {}", id);
                            searchSourceBuilder.query(
                                    QueryBuilders.boolQuery()
                                            .must(QueryBuilders.termQuery("message", search))
                                            .filter(QueryBuilders.rangeQuery("@timestamp")
                                                    .from(String.valueOf(Long.parseLong(time) - 1000000000)) // 시간 Issue 해결 해야 한다.
                                                    .to(String.valueOf(Long.parseLong(time)))));
                            Object[] objects = new Object[]{time, id};
                            searchSourceBuilder.searchAfter(objects);
                            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
                            //TODO 검색어가 있을 경우는 시간이 [현재시간-1000초(16분)] 보다 적은 데이터를 못보여주게 된다. -- EX 컴퓨터 개고수  -- 그러므로 시간을 걸면 안될듯?
//                        searchSourceBuilder.from(Integer.parseInt(String.valueOf(fromCount))); // --> offset --//FIXME 시간 없이 쓰면 too large 에러 난다.
                            searchSourceBuilder.size(50);
                            break;
                        case "down" :
                            log.info("down - search : {}", search);
                            log.info("이전 최신 time = {}" , Long.parseLong(time));
                            log.info("id = {}",id);
                            searchSourceBuilder.query(
                                    QueryBuilders.boolQuery()
                                            .must(QueryBuilders.termQuery("message", search))
                                            .filter(QueryBuilders.rangeQuery("@timestamp")
                                                    .from(String.valueOf(Long.parseLong(time)))
                                                    .to(calendar.getTimeInMillis())));
                            Object[] objectsa = new Object[]{time, id};
                            searchSourceBuilder.searchAfter(objectsa);
                            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
                            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));
                            searchSourceBuilder.size(50);
                            break;
                        case "center" :
                            log.info("center - search : {}", search);
                            log.info("현재시간 = {} ", calendar.getTimeInMillis());
                            searchSourceBuilder.query(
                                    QueryBuilders.boolQuery()
                                            .must(QueryBuilders.termQuery("message", search)));
                            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
                            searchSourceBuilder.size(50);
                            break;
                        default:
                            break;
                    }
                } else {
                    searchSourceBuilder.query(QueryBuilders.matchAllQuery());
                    switch (direction) {
                        case "up":
                            log.info("검색어 없는 Up(위로 Scroll) 구역");
                            log.info("이전 마지막 time = {}" , Long.parseLong(time));
                            log.info("id = {}",id);
                            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
                                    .from(String.valueOf(Long.parseLong(time) - 100000))
                                    .to(String.valueOf(Long.parseLong(time)))); //100초
                            Object[] objects = new Object[]{time, id};
                            searchSourceBuilder.searchAfter(objects);
                            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
                            searchSourceBuilder.size(100);
                            break;
                        case "down":
                            log.info("검색어 없는 Down(아래로 Scroll) 구역");
                            log.info("이전 최신 time = {}" , Long.parseLong(time));
                            log.info("id = {}",id);
                            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
                                    .from(String.valueOf(Long.parseLong(time)))
                                    .to(calendar.getTimeInMillis()));
                            Object[] objectsa = new Object[]{time, id};
                            searchSourceBuilder.searchAfter(objectsa);
                            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
                            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.ASC));
                            searchSourceBuilder.size(100);
                            break;
                        case "center":
                            log.info("검색어 없는 Center 구역");
                            log.info("현재시간 = {} ", calendar.getTimeInMillis());
                            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
//                                .from(String.valueOf(calendar.getTimeInMillis() - 30000))
                                    .to(calendar.getTimeInMillis())); // -30초 ~ 현재 시간
                            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
                            searchSourceBuilder.size(100);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

    @Override
    public SearchResponse getDocumentDetail(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String id) throws IOException {
        return client.search(searchRequest.source(searchSourceBuilder.query(QueryBuilders.termsQuery("_id", id))), RequestOptions.DEFAULT);
    }

    @Override
    public SearchResponse getStreamBigData(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String direction, String hostName, String time, String search, String id) throws IOException {


        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        String[] includeFields = new String[] {"@timestamp", "input", "message"};
        String[] excludeFields = new String[] {};
        searchSourceBuilder.fetchSource(includeFields, excludeFields);

        if (search != null) {
            log.info("center STREAM - search : {}", search);
            log.info("현재시간 = {} ", calendar.getTimeInMillis());
            searchSourceBuilder.query(
                    QueryBuilders.boolQuery()
                            .must(QueryBuilders.termQuery("message", search)));
            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
            searchSourceBuilder.size(100);
        } else {
            log.info("검색어 없는 Center STREAM 구역");
            log.info("현재시간 = {} ", calendar.getTimeInMillis());
            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
//                                .from(String.valueOf(calendar.getTimeInMillis() - 30000))
                    .to(calendar.getTimeInMillis())); // -30초 ~ 현재 시간
            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
            searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
            searchSourceBuilder.size(100);
        }


        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }
}


//            if (direction.equals("up")) {
//                log.info("up - 라이브스트림 : {}", search);
//                log.info("이전 마지막 time = {}" , Long.parseLong(time));
//                log.info("id = {}", id);
//                searchSourceBuilder.query(
//                        QueryBuilders.boolQuery()
//                                .must(QueryBuilders.matchAllQuery())
//                                .filter(QueryBuilders.rangeQuery("@timestamp")
//                                        .from(String.valueOf(Long.parseLong(time) - 1000000000))
//                                        .to(String.valueOf(Long.parseLong(time)))));
//                Object[] objects = new Object[]{time, id};
//                searchSourceBuilder.searchAfter(objects);
//                searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
//                searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
//                searchSourceBuilder.size(200);
//            } else {
//                /**
//                 * 1. LiveStream 버튼
//                 * 데이터가 적을떄,는 붙이지만, 많을때는 짜른다.
//                 */
//                log.info("라이브스트림 이지만 Down 에 속한다.");
//                log.info("이전 마지막 time = {}" , Long.parseLong(time));
//                log.info("id = {}", id);
////                searchSourceBuilder.query(
////                        QueryBuilders.boolQuery()
////                                .must(QueryBuilders.matchAllQuery())
////                                .filter(QueryBuilders.rangeQuery("@timestamp")
////                                        .from(time)
////                                        .to(calendar.getTimeInMillis())));
//                searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(time)).to(calendar.getTimeInMillis()));
//                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//                Object[] objects = new Object[]{time,id};
//                searchSourceBuilder.searchAfter(objects);
//                searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
//                searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
//                searchSourceBuilder.size(200);
//            }