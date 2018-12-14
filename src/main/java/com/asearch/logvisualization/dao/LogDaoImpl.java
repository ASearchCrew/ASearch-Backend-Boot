package com.asearch.logvisualization.dao;

import io.micrometer.core.lang.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

@Slf4j
@Repository
public class LogDaoImpl extends BaseDaoImpl implements LogDao {


    //TODO 1. 중복 코드 줄일 것 /  2. 아래 코드들이 Dao 에서 적절한가?
    @Override
    public SearchResponse getLogs(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String direction, String time, @Nullable String search, boolean isStream) throws IOException {
        log.info(search + "--------------------------------------------------------------------");
        /**
         *
         * 1. up 일시
         *    from = currentTime (화면에서 보여지는 마지막 Item 의 시간)
         *    to = -10s ( 화면에서 보여지는 마지막 Item 의 시간 +
         *
         * 2. down 일시
         *    from = currentTime (화면에서 보여지는 최근 Item 의 시간)
         *    to = +10s ( 화면에서 보여지는 마지막 Item 의 시간 +
         *
         *   issue :: 화면 down 일시
         *
         * 3. center 일시
         *
         *
         *  //&search=null
         */

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());


        int tempCur;
//        tempCur = count * 5;
//        searchSourceBuilder.from(0);
//        searchSourceBuilder.size(4);

        String[] includeFields = new String[] {"@timestamp", "input", "message"};
        String[] excludeFields = new String[] {};
        searchSourceBuilder.fetchSource(includeFields, excludeFields);

        String changedTime = null;

        if (isStream) {
            searchSourceBuilder.size(100);
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(Long.parseLong(time) + 10)).to(calendar.getTimeInMillis()));
            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
//            searchSourceBuilder.size(100);
        } else {
            if (search != null) {
                searchSourceBuilder.query(QueryBuilders.matchQuery("message", search));
                switch (direction) {
                    case "down" :
                        log.info("down - search : {}", search);
                        log.info("time = {}" , Long.parseLong(time));
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
                        searchSourceBuilder.size(2);
                        searchSourceBuilder.query(
                                QueryBuilders.boolQuery()
                                .must(QueryBuilders.termQuery("message", search))
                                .filter(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(Long.parseLong(time))).to(calendar.getTimeInMillis())));
//                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(Long.parseLong(time) + 10)).to(calendar.getTimeInMillis()));
//                        searchSourceBuilder.query(QueryBuilders.matchQuery("message", search));
                        break;
                    case "up" :
                        log.info("up - search : {}", search);
                        log.info("time = {}" , Long.parseLong(time));
                        searchSourceBuilder.size(2);
                        searchSourceBuilder.query(
                                QueryBuilders.boolQuery()
                                        .must(QueryBuilders.termQuery("message", search))
                                        .filter(QueryBuilders.rangeQuery("@timestamp").to(String.valueOf(Long.parseLong(time)))));
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));


//                        searchSourceBuilder.query(QueryBuilders.matchQuery("message", search));
//                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").to(String.valueOf(Long.parseLong(time) - 10)));
                        break;
                    case "center" :
                        log.info("center - search : {}", search);
                        log.info("time = {}" , Long.parseLong(time));
//                    searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(calendar.getTimeInMillis() - 300000)).to(calendar.getTimeInMillis()));
                        searchSourceBuilder.query(
                                QueryBuilders.boolQuery()
                                        .must(QueryBuilders.termQuery("message", search)));
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                        searchSourceBuilder.size(2);
//                        searchSourceBuilder.query(QueryBuilders.matchQuery("message", search));
                        break;
                    default:
                        break;
                }

            } else {
                searchSourceBuilder.size(4);
                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
                switch (direction) {
                    case "down":
                        log.info("NoSearch Down");
                        log.info("time = {}" , Long.parseLong(time));
//                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(Long.parseLong(time) + 10)).to(String.valueOf(Long.parseLong(time) + 1000000)));
                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(Long.parseLong(time) + 10)).to(calendar.getTimeInMillis()));
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
                        break;
                    case "up":
                        log.info("NoSearch Up");
                        log.info("time = {}" , Long.parseLong(time));
                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(Long.parseLong(time) - 1000000)).to(String.valueOf(Long.parseLong(time) - 10)));
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                        break;
                    case "center":
                        log.info("NoSearch Center");
                        log.info("Calender - Time in milliseconds : " + calendar.getTimeInMillis());
                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(calendar.getTimeInMillis() - 300000)).to(calendar.getTimeInMillis()));
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
                        break;
                    default:
                        break;
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
}
