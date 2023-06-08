package kz.greetgo.sandboxserver.elastic;

import kz.greetgo.sandboxserver.elastic.model.EsBodyWrapper;
import kz.greetgo.sandboxserver.model.Paging;
import kz.greetgo.sandboxserver.util.jackson.ObjectMapperHolder;
import lombok.SneakyThrows;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class ElasticWorkerImpl implements InitializingBean, DisposableBean, ElasticWorker {

  @Value("${sandbox.elastic.schema}")
  private String schema;

  @Value("${sandbox.elastic.host}")
  private String host;

  @Value("${sandbox.elastic.port}")
  private int port;

  private RestClient restClient;

  @Override
  public void afterPropertiesSet() {
    restClient = RestClient.builder(
      new HttpHost(host, port, schema)
    ).build();

  }

  @Override
  public void destroy() throws Exception {
    if (restClient != null) {
      restClient.close();
    }
  }

  @Override
  public Response performRequest(Request request) {
    try {
      return restClient.performRequest(request);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Response createIndex(String indexName, String mapping) {
    Request request = new Request("PUT", "/" + indexName);

    request.setJsonEntity(mapping);

    return performRequest(request);
  }

  @Override
  public boolean doesIndexExists(String indexName) {
    Request request = new Request("HEAD", "/" + indexName);
    Response response = performRequest(request);
    return response.getStatusLine().getStatusCode() == 200;
  }

  @SneakyThrows
  @Override
  public EsBodyWrapper findAll(String indexName, Paging paging) {
    Request request = new Request("GET", "/" + indexName + "/_search");

    String searchQuery = "{\"query\": {\"match_all\": {}}, \"size\": " + paging.limit + ", \"from\": " + paging.offset + "}";

    HttpEntity entity = new NStringEntity(searchQuery, ContentType.APPLICATION_JSON);
    request.setEntity(entity);

    Response response = performRequest(request);

    String body = EntityUtils.toString(response.getEntity());

    EsBodyWrapper bodyWrapper = ObjectMapperHolder.readJson(body, EsBodyWrapper.class);

    if (bodyWrapper.timed_out) {
      throw new RuntimeException("Request to elastic has been timed out");
    }

    return bodyWrapper;
  }

  @SneakyThrows
  @Override
  public EsBodyWrapper find(String indexName, Map<String, String> valueMap, Paging paging) {
    if (valueMap.isEmpty()) {
      return findAll(indexName, paging);
    }

    StringBuilder filterQueryBuilder = new StringBuilder("{\"query\": {\"bool\": {\"filter\": [");

    for (Map.Entry<String, String> entry : valueMap.entrySet()) {
      String field = entry.getKey();
      String value = entry.getValue();

      filterQueryBuilder.append("{\"term\": {\"").append(field).append("\": \"").append(value).append("\"}},");
    }

    // Remove the trailing comma
    filterQueryBuilder.deleteCharAt(filterQueryBuilder.length() - 1);

    filterQueryBuilder.append("]}}}");

    String requestBody = filterQueryBuilder.toString();

    Request request = new Request("POST", "/" + indexName + "/_search");

    request.setJsonEntity(requestBody);

    Response response = performRequest(request);

    String body = EntityUtils.toString(response.getEntity());

    EsBodyWrapper bodyWrapper = ObjectMapperHolder.readJson(body, EsBodyWrapper.class);

    if (bodyWrapper.timed_out) {
      throw new RuntimeException("Request to elastic has been timed out");
    }

    return bodyWrapper;
  }

  @Override
  public Response insertDocument(String indexName, String documentId, String jsonifiedString) {
    Request request = new Request("POST", "/" + indexName + "/_doc/" + documentId);
    HttpEntity entity = new NStringEntity(jsonifiedString, ContentType.APPLICATION_JSON);
    request.setEntity(entity);

    return performRequest(request);
  }

  @Override
  public Response updateDocument(String indexName, String documentId, String jsonifiedString) {
    Request request = new Request("PUT", "/" + indexName + "/_doc/" + documentId);
    HttpEntity entity = new NStringEntity(jsonifiedString, ContentType.APPLICATION_JSON);
    request.setEntity(entity);

    return performRequest(request);
  }

  @Override
  public Response deleteDocument(String indexName, String documentId) {
    Request request = new Request("DELETE", "/" + indexName + "/_doc/" + documentId);

    return performRequest(request);
  }

}
