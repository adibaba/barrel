package de.adrianwilke.barrel;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

/**
 * Elasticsearch access.
 *
 * @author Adrian Wilke
 */
public class Elasticsearch {

	protected static final Logger LOGGER = LogManager.getLogger();

	protected static void write(HttpHost httpHost, String index, String id, String text) throws IOException {
		try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(httpHost))) {
			IndexRequest indexRequest = new IndexRequest(index).id(id).source("text", text);

			IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
			if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
				LOGGER.info(DocWriteResponse.Result.CREATED + " " + id);
			} else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
				LOGGER.info(DocWriteResponse.Result.UPDATED + " " + id);
			}
		}
	}

	protected static SearchResponse search(HttpHost httpHost, String index, String text) throws IOException {
		try (RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(httpHost))) {

			QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("text", text).fuzziness(Fuzziness.AUTO)
					.prefixLength(3).maxExpansions(10);

			HighlightBuilder highlightBuilder = new HighlightBuilder();
			HighlightBuilder.Field highlightField = new HighlightBuilder.Field("text");
			highlightBuilder.field(highlightField);

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.query(matchQueryBuilder);
			sourceBuilder.highlighter(highlightBuilder);
			sourceBuilder.size(100);

			SearchRequest searchRequest = new SearchRequest(index);
			searchRequest.source(sourceBuilder);

			return client.search(searchRequest, RequestOptions.DEFAULT);
		}
	}
}