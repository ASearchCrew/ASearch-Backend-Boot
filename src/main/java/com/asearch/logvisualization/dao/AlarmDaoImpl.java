package com.asearch.logvisualization.dao;

import com.asearch.logvisualization.service.BaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Map;

import static com.asearch.logvisualization.util.Constant.MANAGEMENT_SERVER_INDEX;
import static com.asearch.logvisualization.util.Constant.MANAGEMENT_SERVER_TYPE;

@Slf4j
@Repository
public class AlarmDaoImpl extends BaseDaoImpl implements AlarmDao {

    @Override
    public GetResponse getExistedKeywords(GetRequest getRequest,
                                          String field,
                                          String content) throws IOException {

        String[] includes = new String[]{field};
        String[] excludes = Strings.EMPTY_ARRAY;
        FetchSourceContext fetchSourceContext =
                new FetchSourceContext(true, includes, excludes);
//        getRequest.fetchSourceContext(fetchSourceContext);

        return client.get(getRequest, RequestOptions.DEFAULT);
    }


    @Override
    public IndexResponse indexNewKeyword(IndexRequest indexRequest) throws IOException {
        return client.index(indexRequest, RequestOptions.DEFAULT);
    }


    @Override
    public SearchHit[] getKeywordList(SearchRequest searchRequest, SearchSourceBuilder searchSourceBuilder, String where, int size) throws IOException {
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder.size(size);
        searchRequest.source(searchSourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT).getHits().getHits();
    }


    @Override
    public DeleteResponse removeKeywordDocument(DeleteRequest deleteRequest) throws IOException {
        return client.delete(deleteRequest, RequestOptions.DEFAULT);
    }

    @Override
    public UpdateResponse makeNewKeywords(Map<String, Object> parameters, UpdateRequest updateRequest) throws IOException {
        Script inline = new Script(ScriptType.INLINE, "painless",
                "ctx._source.keywords = params.keywords", parameters);
        updateRequest.script(inline);
        return client.update(updateRequest, RequestOptions.DEFAULT);
    }

    @Override
    public UpdateResponse addKeyword(Map<String, Object> parameters, UpdateRequest updateRequest) throws IOException {
        Script inline = new Script(ScriptType.INLINE, "painless",
                "ctx._source.keywords.add(params.keyword)", parameters);
        updateRequest.script(inline);
        return client.update(updateRequest, RequestOptions.DEFAULT);
    }

    @Override
    public UpdateResponse removeKeyword(Map<String, Object> parameters, UpdateRequest updateRequest, int position) throws IOException {
        String idOrCode = String.format("ctx._source.keywords.remove(%s)", position);
        Script inline = new Script(ScriptType.INLINE, "painless",
                idOrCode, parameters);
        updateRequest.script(inline);
        return client.update(updateRequest, RequestOptions.DEFAULT);
    }
}
