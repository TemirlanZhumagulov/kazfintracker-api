package kz.greetgo.sandboxserver.elastic;

import kz.greetgo.sandboxserver.elastic.model.EsBodyWrapper;
import kz.greetgo.sandboxserver.model.Paging;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import java.util.Map;

public interface ElasticWorker {

  Response performRequest(Request request);

  Response createIndex(String indexName, String mapping);

  boolean doesIndexExists(String indexName);

  EsBodyWrapper findAll(String indexName, Paging paging);

  EsBodyWrapper find(String indexName, Map<String, String> valueMap, Paging paging);

  Response insertDocument(String indexName, String documentId, String jsonifiedString);

  Response updateDocument(String indexName, String documentId, String jsonifiedString);

  Response deleteDocument(String indexName, String documentId);

}
