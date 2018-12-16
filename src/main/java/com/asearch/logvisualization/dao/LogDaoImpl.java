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
                                  @Nullable String id) throws IOException {
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

        /**
         *  LiveStream 을 켰을시.
         */
        if (isStream) {
            searchSourceBuilder.size(100);
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(Long.parseLong(time) + 10)).to(calendar.getTimeInMillis()));
            searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
//            searchSourceBuilder.size(100);
        } else {
            /**
             *  검색어(Search) 가 있을떄
             */
            if (search != null) {
                searchSourceBuilder.query(QueryBuilders.matchQuery("message", search));
                switch (direction) {
                    case "up" :
//                        log.info(initialCount);
                        //TODO 1. 갯수가 500000개가 넘어갈시에는, 짤라서 계산해야 한다. 변수가 한개 필요 -- 500000개 미만일시 그냥 계산하기.
                        /**
                         * initialCount 는 : 0 <-- 이 안나오기 위해서 center Request 를 받았을 떄부터, localStarage 에 저장 해놓아야 한다.
                         */
                        log.info("up - search : {}", search);
                        log.info("time = {}" , Long.parseLong(time));
//                        if (initialCount - (upScrollOffset*15) <= 0) {
//
//                        }
                        log.info("initialCount 는 : {}", initialCount);
                        long fromCount = initialCount - (upScrollOffset*15);
                        log.info("fromCount = {}", fromCount);
//                        log.info("a " + from);
//                        if (from >= 0) {
//                            log.info("0보다 크다.");
//                            searchSourceBuilder.from(Integer.parseInt(String.valueOf(from)));
//                            searchSourceBuilder.size(15);
//                        }


//                        searchSourceBuilder.query(
//                                QueryBuilders.boolQuery()
//                                        .must(QueryBuilders.termQuery("message", search)));
//                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));


//                        searchSourceBuilder.query(QueryBuilders.matchQuery("message", search));
//                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").to(String.valueOf(Long.parseLong(time) - 10)));

                        //TODO 검색어가 있을 경우는 시간이 [현재시간-1000초(16분)] 보다 적은 데이터를 못보여주게 된다. -- EX 컴퓨터 개고수
                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(Long.parseLong(time) - 1000000)).to(String.valueOf(Long.parseLong(time))));
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
//                        searchSourceBuilder.sort(new FieldSortBuilder("offset").order(SortOrder.DESC));

                        searchSourceBuilder.from(Integer.parseInt(String.valueOf(fromCount))); // --> offset --//FIXME 시간 없이 쓰면 too large 에러 난다.
//                        searchSourceBuilder.from(0);
                        searchSourceBuilder.size(15);
                        break;
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
                    case "center" :
                        log.info("center - search : {}", search);
                        log.info("time = {}" , Long.parseLong(time));
//                    searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(calendar.getTimeInMillis() - 300000)).to(calendar.getTimeInMillis()));
                        searchSourceBuilder.query(
                                QueryBuilders.boolQuery()
                                        .must(QueryBuilders.termQuery("message", search)));
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
                        searchSourceBuilder.size(15);
//                        searchSourceBuilder.query(QueryBuilders.matchQuery("message", search));
                        break;
                    default:
                        break;
                }

            } else {
                /**
                 *  검색어(Search) 가 없을때 -- 가장 일반적인 상황.
                 */
                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
                /**
                 *  Direction 별 조건문
                 *
                 *  - up 은 offset 계산이 필요하고
                 *  - down 은 전체 count 요청을 한번 더해서 계한 해야 한다.
                 *
                 */
                switch (direction) {
                    case "up":
                        log.info("검색어 없는 Up(위로 Scroll) 구역");
                        log.info("time = {}" , Long.parseLong(time));
                        //TODO 로그 찍기 parameter cannot be negative 에러를 찾기위해서  -- 음수는 안된다는게 무슨 말이지 모르겠다.
                        /**
                         * - initialCount 는 한단계 전의 response.sumCount 로 계속 바뀐다. (왜냐하면 시간으로 범위를 지정했기 때문에?)
                         * - 만약,
                         */
                        long fromCount = initialCount - (upScrollOffset * 5);
                        log.info("initialCount = {}", initialCount);
                        log.info("fromCount = {}", fromCount);

                        log.info("id = {}",id);
//                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
//                        Date date = dateFormat.parse(time);
//                        calendar.setTime(date);

//                        searchSourceBuilder.query(
//                                QueryBuilders.boolQuery()
//                                        .filter(QueryBuilders.rangeQuery("@timestamp")
//                                                .gte(Long.parseLong(time) - 1000000)
//                                                .lte(Long.parseLong(time) - 10)));


//                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
//                                .gte(String.valueOf(Long.parseLong(time) - 1000000))
//                                .lte(String.valueOf(Long.parseLong(time) - 10 )));
//                        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
//                        log.info(String.valueOf(fromCount));

                        //TODO 검색어가 있을 경우는 시간이 [현재시간-1000초(16분)] 보다 적은 데이터를 못보여주게 된다. -- EX 컴퓨터 개고수
                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(Long.parseLong(time) - 1000000)).to(String.valueOf(Long.parseLong(time))));
                        Object[] objects = new Object[]{time};
                        searchSourceBuilder.searchAfter(objects);
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
//                        searchSourceBuilder.sort(new FieldSortBuilder("_id").order(SortOrder.DESC));
                        //TODO searchAfter 에서 timeStamp 만 이용하게 되면 logData 가 누락된다.

                        /**
                         * 총 9000000 (900만 개)
                         * 100000 (10만 개) - center
                         * ====
                         * 1000개 -- up
                         */
//                        searchSourceBuilder.from(Integer.parseInt(String.valueOf(fromCount))); // --> offset --//FIXME 시간 없이 쓰면 too large 에러 난다.
//                        searchSourceBuilder.from(0);
                        searchSourceBuilder.size(5);
//                        searchRequest.scroll(TimeValue.timeValueMinutes(1L));

//                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(Long.parseLong(time) - 1000000)).to(String.valueOf(Long.parseLong(time) - 10)));
                        break;
                    case "down":
                        log.info("검색어 없는 Down(아래로 Scroll) 구역");
                        log.info("time = {}" , Long.parseLong(time));

//                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(Long.parseLong(time) + 10)).to(String.valueOf(Long.parseLong(time) + 1000000)));
                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(Long.parseLong(time) + 10)).to(calendar.getTimeInMillis()));
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
                        break;
                    case "center":
                        log.info("검색어 없는 Center 구역");
                        log.info("Calender - Time in milliseconds : " + calendar.getTimeInMillis());

                        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp").from(String.valueOf(calendar.getTimeInMillis() - 30000)).to(calendar.getTimeInMillis()));
                        /**
                         * 시간 범위를 지정 하지 않으면 전체 count 를 가져오고, 지정하면 지정한만큼의 count 만 들고 오게 된다.
                         */
                        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.ASC));
                        /**
                         *  처음은 시간 범위를 넣지 않고 DESC 로 해도 되지만 -- 로그가 연속적으로 계속 쌓인다는 가정이고실제로도 그렇기 때문에,
                         *  시간범위 30초 사이의 데이터를 ASC 로 가져왔다. (//TODO ASC 를 DESC 로 바꾸자. 왜냐하면, ASC로 하면 가장 최근 데이터가 짤릴수도 있기 때문에.
                         */
                        searchSourceBuilder.size(5);
//                        searchSourceBuilder.query(
//                                QueryBuilders.rangeQuery("@timestamp").
//                                from(String.valueOf(calendar.getTimeInMillis() - 30000)). // 30초
//                                to(calendar.getTimeInMillis()));  // 현재시간
                        break;
                    default:
                        break;
                }


            }

        }
        /**
         *  Filter 를 거친 최종 RequestQuery 이다.
         */
        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

    @Override
    public SearchResponse getDocumentDetail(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String id) throws IOException {

        return client.search(searchRequest.source(searchSourceBuilder.query(QueryBuilders.termsQuery("_id", id))), RequestOptions.DEFAULT);
    }
}
