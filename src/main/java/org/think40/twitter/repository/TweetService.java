package org.think40.twitter.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.think40.twitter.api.Tweet;
import org.think40.twitter.api.TweetApi;
import org.think40.twitter.api.TweetListResult;

import java.io.IOException;
import java.util.*;

@Service
public class TweetService implements TweetApi {

    private final String INDEX = "tweet_data";
    private final String TYPE = "tweets";
    private final Logger logger = LoggerFactory.getLogger(TweetService.class);

    private RestHighLevelClient client;
    private ObjectMapper objectMapper;

    @Autowired
    public TweetService(RestHighLevelClient client, ObjectMapper objectMapper) {
        this.client = client;
        this.objectMapper = objectMapper;
    }

    public boolean ping() {
        try {
            return client.ping(RequestOptions.DEFAULT);
        } catch (IOException e) {
            logger.info("Ping failed");
            logger.debug("Ping failed details", e);
        }
        return false;
    }

    public void initializeIndex() {
        boolean exists = indexExists();
        if (!exists) createIndex();
    }

    private boolean indexExists() {
        IndicesClient indicesClient = client.indices();
        GetIndexRequest request = new GetIndexRequest().indices(INDEX);
        boolean exists = false;
        try {
            exists = indicesClient.exists(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            logger.error("Existence check failed", e);
        }
        logger.info("{} exists: {}", INDEX, exists);
        return exists;
    }

    private void createIndex() {
        CreateIndexRequest request = new CreateIndexRequest(INDEX)
                .source("{\"settings\" : {\n" +
                        "  \"index\" : {\n" +
                        "    \"analysis\": {\n" +
                        "      \"analyzer\": {\n" +
                        "        \"default\": {\n" +
                        "          \"type\": \"whitespace\",\n" +
                        "          \"tokenizer\": \"whitespace\"\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}}", XContentType.JSON);
        request.mapping(TYPE, "creationDate", "type=date");
        IndicesClient indicesClient = client.indices();
        try {
            indicesClient.create(request, RequestOptions.DEFAULT);
            logger.info("index {} created", INDEX);

        } catch (Exception e) {
            logger.error("Unable to create index {}", INDEX, e);
        }
    }

    public Tweet createDocument(Tweet doc) throws IOException {
        Map dataMap = objectMapper.convertValue(doc, Map.class);
        IndexRequest indexRequest = new IndexRequest(INDEX, TYPE)
                .source(dataMap);
        IndexResponse response = client.index(indexRequest, RequestOptions.DEFAULT);
        logger.debug("created {}", response);
        doc.setId(response.getId());
        return doc;
    }

    public TweetListResult searchInMessage(String tag, Integer offset, Integer limit) throws IOException {
        logger.trace("searchBy tag:{} offset:{} limit:{}", tag, offset, limit);
        SearchRequest searchRequest = prepareRequest(tag, offset, limit);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        TweetListResult result = parseResponse(searchResponse);
        result.getPaging().setLimit(limit);
        result.getPaging().setOffset(offset);
        return result;
    }

    private SearchRequest prepareRequest(String tag, Integer offset, Integer limit) {
        SearchRequest searchRequest = new SearchRequest(INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        if (tag == null)
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        else
            searchSourceBuilder.query(QueryBuilders.matchPhraseQuery("message", tag));
        searchSourceBuilder.from(offset);
        searchSourceBuilder.size(limit);
        searchSourceBuilder.sort("creationDate", SortOrder.DESC);
        searchRequest.source(searchSourceBuilder);
        return searchRequest;
    }

    private TweetListResult parseResponse(SearchResponse searchResponse) throws IOException {
        TweetListResult result = new TweetListResult();
        result.getPaging().setTotalCount(searchResponse.getHits().totalHits);
        SearchHit[] hits = searchResponse.getHits().getHits();
        parseHits(result, hits);
        return result;
    }

    private void parseHits(TweetListResult result, SearchHit[] hits) throws IOException {
        for (SearchHit hit : hits) {
            String source = hit.getSourceAsString();
            Tweet item = objectMapper.readValue(source, Tweet.class);
            item.setId(hit.getId());
            result.getTweets().add(item);
        }
    }
}
