package kz.greetgo.sandboxserver.elastic;

import kz.greetgo.sandboxserver.elastic.model.EsBodyWrapper;
import kz.greetgo.sandboxserver.model.Paging;
import kz.greetgo.sandboxserver.model.web.ClientsTableRequest;
import kz.greetgo.sandboxserver.model.web.TableRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import java.util.Map;

public interface ElasticWorker {

  Response performRequest(Request request);

  Response createIndex(String indexName, String mapping);

  Response refresh(String indexName);

  boolean doesIndexExists(String indexName);

  EsBodyWrapper findAll(String indexName, Paging paging);

  EsBodyWrapper find(String indexName, ClientsTableRequest tableRequest, Paging paging);
  EsBodyWrapper findModel(String indexName, Map<String,String> valueMap, Paging paging);

  Response insertDocument(String indexName, String documentId, String jsonifiedString);

  Response updateDocument(String indexName, String documentId, String jsonifiedString);

  Response deleteDocument(String indexName, String documentId);

  int getClientListAll(String indexClient);
}
