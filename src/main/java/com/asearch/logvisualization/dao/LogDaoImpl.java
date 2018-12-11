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

    @Override
    public SearchResponse getLogs(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String direction, String time, @Nullable String search) throws IOException {

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
         */
        int tempCur;
//        tempCur = count * 5;
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(500);

        String[] includeFields = new String[] {"@timestamp", "input", "message"};
        String[] excludeFields = new String[] {};
        searchSourceBuilder.fetchSource(includeFields, excludeFields);

        String changedTime = null;

        //TODO 1. 중복 코드 줄일 것 /  2. 아래 코드들이 Dao 에서 적절한가?
//&search=null

        //TODO search 이건 전체 검색어로 해야 한다.
        if (search != null) {
            searchSourceBuilder.query(QueryBuilders.termQuery("message", search));
        }


        if (direction.equals("down")) {
            log.info(Long.parseLong(time) + 10000 + "  ----");
            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(time).to(String.valueOf(Long.parseLong(time) + 10000)));
            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));

        } else if (direction.equals("up")) {

            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(time).to(String.valueOf(Long.parseLong(time) - 10000)));
            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));

        } else if (direction.equals("center")) {

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
//            System.out.println("Calender - Time in milliseconds : " + calendar.getTimeInMillis());
            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(calendar.getTimeInMillis() - 300000)).to(calendar.getTimeInMillis()));
            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
        }

        searchRequest.source(searchSourceBuilder);

        return client.search(searchRequest, RequestOptions.DEFAULT);
    }
}
