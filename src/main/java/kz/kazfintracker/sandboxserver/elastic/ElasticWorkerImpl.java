package kz.kazfintracker.sandboxserver.elastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.kazfintracker.sandboxserver.config.ElasticConfig;
import kz.kazfintracker.sandboxserver.elastic.model.CountWrapper;
import kz.kazfintracker.sandboxserver.elastic.model.EsBodyWrapper;
import kz.kazfintracker.sandboxserver.model.web.ClientsTableRequest;
import kz.kazfintracker.sandboxserver.model.web.Paging;
import kz.kazfintracker.sandboxserver.util.jackson.ObjectMapperHolder;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class ElasticWorkerImpl implements InitializingBean, DisposableBean, ElasticWorker {

    @Value("${sandbox.elastic.schema}")
    private String schema;

    @Value("${sandbox.elastic.host}")
    private String host;

    @Value("${sandbox.elastic.port}")
    private int port;

    private RestClient restClient;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private ElasticConfig elasticConfig;

    @NotNull
    private static EsBodyWrapper parseGetResponse(String body) {

        EsBodyWrapper bodyWrapper = ObjectMapperHolder.readElastic(body, EsBodyWrapper.class);
        log.info("RfT4SHamFd :: CREATED EsBodyWrapper OBJECT " + bodyWrapper);

        if (bodyWrapper.timed_out) {
            throw new RuntimeException("Request to elastic has been timed out");
        }

        return bodyWrapper;
    }

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
        log.info("WILL CREATE INDEX client with the following mapping: " + mapping);
        Request request = new Request("PUT", "/" + indexName);

        request.setJsonEntity(mapping);
        log.info("REQUEST TO CREATE INDEX IS SENT: " + request);
        return performRequest(request);
    }

    @Override
    public Response refresh(String indexName) {
        Request request = new Request("POST", "/_refresh");

        return performRequest(request);
    }

    @Override
    public boolean doesIndexExists(String indexName) {
        Request request = new Request("HEAD", "/" + indexName);
        Response response = performRequest(request);
        log.info("CHECK IF THE INDEX client EXISTS: " + response.getStatusLine().getStatusCode());
        return response.getStatusLine().getStatusCode() == 200;
    }

    @SneakyThrows
    public String searchDocuments(String indexName, String searchQuery) {
        Request request = new Request("GET", "/" + indexName + "/_search");

        HttpEntity entity = new NStringEntity(searchQuery, ContentType.APPLICATION_JSON);

        request.setEntity(entity);

        log.info("5Cy0CQ3N7I :: Request is ready to be performed by rest API to elastic" + request);

        Response response = performRequest(request);

        return EntityUtils.toString(response.getEntity());
    }

    @SneakyThrows
    @Override
    public EsBodyWrapper findAll(String indexName, Paging paging) {

        String searchQuery = "{\"query\": {\"match_all\": {}}, \"size\": " + paging.limit + ", \"from\": " + paging.offset + ",\"track_total_hits\": true}";

        String response = searchDocuments(indexName, searchQuery);
        log.info("5Cy0CQ3N7I :: Response from rest API to elastic" + response);

        return parseGetResponse(response);
    }

    @SneakyThrows
    public double sumFieldWithQuery(String indexName, String fieldName, String query) {
        String queryJson = "{\n" +
                "  \"query\": {\n" +
                "    \"query_string\": {\n" +
                "      \"query\": \"" + query + "\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"aggs\": {\n" +
                "    \"field_sum\": {\n" +
                "      \"sum\": {\n" +
                "        \"field\": \"" + fieldName + "\"\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"size\": 0\n" +
                "}";

        String responseBody = searchDocuments(indexName, queryJson);

        return parseFieldSumResponse(responseBody);
    }

    private double parseFieldSumResponse(String responseBody) {
        JsonNode jsonNode = ObjectMapperHolder.readTree(responseBody);
        if (jsonNode.path("timed_out").asBoolean()) {
            throw new RuntimeException("Request to Elasticsearch timed out");
        }
        return jsonNode.path("aggregations").path("field_sum").path("value").asDouble();
    }

    // region find with request

    @SneakyThrows
    @Override
    public EsBodyWrapper find(String indexName, ClientsTableRequest tableRequest, Paging paging) {
        Map<String, String> valueMap = tableRequest.toMap();
        if (valueMap.isEmpty() && tableRequest.sorting == null) {
            log.info("valueMap is empty. There is nothing to filter!");
            return findAll(indexName, paging);
        }
        String requestBody = sortFields(prefixAndMiddleMatch(valueMap), tableRequest.sorting);
        requestBody += "\"size\": " + paging.limit + ", \"from\": " + paging.offset + ",\"track_total_hits\": true}";
        log.info("requestBody is made: " + requestBody);

        Request request = new Request("POST", "/" + indexName + "/_search");

        request.setJsonEntity(requestBody);
        log.info("request is made: " + request);

        Response response = performRequest(request);

        String body = EntityUtils.toString(response.getEntity());
        log.info("JSON response is received: " + body);
        EsBodyWrapper bodyWrapper = ObjectMapperHolder.readJson(body, EsBodyWrapper.class);

        if (bodyWrapper.timed_out) {
            throw new RuntimeException("Request to elastic has been timed out");
        }

        return bodyWrapper;
    }

    @SneakyThrows
    @Override
    public EsBodyWrapper findModel(String indexName, Map<String, String> valueMap, Paging paging) {
        if (valueMap.isEmpty()) {
            return findAll(indexName, paging);
        }

        String requestBody = prefixMatch(valueMap);

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


    /**
     * Метод использует should и находит матчы (match) по префиксу и
     * по wildcardy (что позволяет находит матчы по внутри одного слова)
     *
     * @param valueMap ключ - название поля, значение - значения поля
     * @return запрос
     */
    private String prefixAndMiddleMatch(Map<String, String> valueMap) {

        log.info("prefixAndMiddleMatch() is activated");

        if (valueMap == null || valueMap.isEmpty()) {
            return "{\"query\": {\"match_all\": {}},";
        }

        StringBuilder filterQueryBuilder = new StringBuilder("{\"query\": {\"bool\": {\"should\": [");

        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            String field = entry.getKey();
            String value = entry.getValue();
//            if(entry.getKey().equals("full_name") || entry.getKey().equals("charm")){
//                filterQueryBuilder.append("{\"match_phrase_prefix\": {\"").append(field).append(".text\": \"").append(value).append("\"}},");
//                filterQueryBuilder.append("{\"wildcard\": {\"").append(field).append(".text\": \"*").append(value).append("*\"}},");
//            } else {
            filterQueryBuilder.append("{\"match_phrase_prefix\": {\"").append(field).append("\": \"").append(value).append("\"}},");
            filterQueryBuilder.append("{\"wildcard\": {\"").append(field).append("\": \"*").append(value).append("*\"}},");
//            }
        }

        // Remove the trailing comma
        filterQueryBuilder.deleteCharAt(filterQueryBuilder.length() - 1);

        filterQueryBuilder.append("]}}, ");
        log.info("prefixAndMiddleMatch() built query:: " + filterQueryBuilder);

        return filterQueryBuilder.toString();
    }

    private String sortFields(String filteredRequestBody, HashMap<String, Boolean> sorting) {
        if (sorting == null) {
            return filteredRequestBody;
        }
        log.info("sortFields() is caused");
        StringBuilder sortQueryBuilder = new StringBuilder(filteredRequestBody + "\"sort\": [");
        for (Map.Entry<String, Boolean> e : sorting.entrySet()) {
            if (e.getKey().equals("full_name") || e.getKey().equals("charm")) {
                log.info("full_name or charm is activated");
                sortQueryBuilder.append("{\"").append(e.getKey()).append(".keyword\": { \"order\": ");
            } else {
                log.info("full_name or charm is NOT activated");
                sortQueryBuilder.append("{\"").append(e.getKey()).append("\": { \"order\": ");
            }
            if (e.getValue()) {
                sortQueryBuilder.append("\"asc\"}},");
            } else {
                sortQueryBuilder.append("\"desc\"}},");
            }
        }
        sortQueryBuilder.deleteCharAt(sortQueryBuilder.length() - 1);
        return sortQueryBuilder.append("],").toString();
    }

    /**
     * Метод использует must и находит матчы (match) по префиксу
     *
     * @param valueMap ключ - название поля, значение - значения поля
     * @return запрос
     */
    private String prefixMatch(Map<String, String> valueMap) {

        if (valueMap == null || valueMap.isEmpty()) {
            throw new RuntimeException("Value map is expected to have at least one value");
        }

        StringBuilder filterQueryBuilder = new StringBuilder("{\"query\": {\"bool\": {\"must\": [");

        for (Map.Entry<String, String> entry : valueMap.entrySet()) {
            String field = entry.getKey();
            String value = entry.getValue();

            filterQueryBuilder.append("{\"match_phrase_prefix\": {\"").append(field).append("\": \"").append(value).append("\"}},");
        }

        // Remove the trailing comma
        filterQueryBuilder.deleteCharAt(filterQueryBuilder.length() - 1);

        filterQueryBuilder.append("]}}}");

        return filterQueryBuilder.toString();
    }

    // endregion find with request

    @Override
    public Response insertDocument(String indexName, String documentId, String jsonifiedString) {
        Request request = new Request("POST", "/" + indexName + "/_doc/" + documentId);
        HttpEntity entity = new NStringEntity(jsonifiedString, ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        Response response = performRequest(request);

        if (elasticConfig.updateImmediately()) {
            refresh(indexName);
        }

        return response;
    }

    @Override
    public Response updateDocument(String indexName, String documentId, String jsonifiedString) {
        Request request = new Request("PUT", "/" + indexName + "/_doc/" + documentId);
        HttpEntity entity = new NStringEntity(jsonifiedString, ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        Response response = performRequest(request);

        if (elasticConfig.updateImmediately()) {
            refresh(indexName);
        }

        return response;
    }

    @Override
    public Response deleteDocument(String indexName, String documentId) {
        Request request = new Request("DELETE", "/" + indexName + "/_doc/" + documentId);
        Response response = performRequest(request);

        if (elasticConfig.updateImmediately()) {
            refresh(indexName);
        }

        return response;
    }

    @SneakyThrows
    @Override
    public int countDocuments(String indexName) {
        Request request = new Request("GET", "/" + indexName + "/_count");

        String searchQuery = "{\"query\": {\"match_all\": {}} }";
        HttpEntity entity = new NStringEntity(searchQuery, ContentType.APPLICATION_JSON);
        request.setEntity(entity);

        Response response = performRequest(request);

        String body = EntityUtils.toString(response.getEntity());
        CountWrapper countWrapper = ObjectMapperHolder.readJson(body, CountWrapper.class);
        return countWrapper.count;

    }

    @Override
    @SneakyThrows
    public double calculateTotalTransactions(String transactionType, String startDate, String endDate) {
        String queryJson = "{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\"match\": {\"type\": \"" + transactionType + "\"}},\n" +
                "        {\"range\": {\"date\": {\"gte\": \"" + startDate + "\", \"lte\": \"" + endDate + "\"}}}\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"aggs\": {\n" +
                "    \"total_amount\": {\"sum\": {\"field\": \"amount\"}}\n" +
                "  },\n" +
                "  \"size\": 0\n" +
                "}";

        String responseBody = searchDocuments(ElasticIndexes.INDEX_TRANSACTION, queryJson);

        log.info("Response from Elasticsearch: {}", responseBody);

        return parseTotalAmount(responseBody);
    }

    @Override
    @SneakyThrows
    public Map<String, Double> fetchTransactionsAsMonthHistorgram(String transactionType, String startDate, String endDate) {
        String queryJson = "{\n" +
                "  \"size\": 0,\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must\": [\n" +
                "        {\"match\": {\"type\": \"" + transactionType + "\"}},\n" +
                "        {\"range\": {\"date\": {\"gte\": \"" + startDate + "\", \"lte\": \"" + endDate + "\"}}}\n" +
                "      ]\n" +
                "    }\n" +
                "  },\n" +
                "  \"aggs\": {\n" +
                "    \"group_by_day\": {\n" +
                "      \"date_histogram\": {\n" +
                "        \"field\": \"date\",\n" +
                "        \"calendar_interval\": \"day\"\n" +
                "      },\n" +
                "      \"aggs\": {\n" +
                "        \"total_amount\": {\n" +
                "          \"sum\": {\n" +
                "            \"field\": \"amount\"\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";

        String responseBody = searchDocuments(ElasticIndexes.INDEX_TRANSACTION, queryJson);

        log.info("Response from Elasticsearch: {}", responseBody);

        return parseElasticsearchResponse(responseBody);
    }

    public Map<String, Double> parseElasticsearchResponse(String jsonResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResponse);
        JsonNode buckets = rootNode.path("aggregations").path("group_by_day").path("buckets");
        Map<String, Double> dailyTotals = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");

        for (JsonNode bucket : buckets) {
            String keyAsString = bucket.get("key_as_string").asText();  // This should be the date in yyyy-MM-dd format
            double totalAmount = bucket.path("total_amount").path("value").asDouble();
            LocalDate date = OffsetDateTime.parse(keyAsString).toLocalDate(); // Correctly parse the ISO date-time string to LocalDate

            String dayOfMonth = date.format(formatter); // Format to only show the day of the month


            dailyTotals.put(dayOfMonth, totalAmount);
        }

        return dailyTotals;
    }


    private double parseTotalAmount(String responseBody) {
        JsonNode jsonNode = ObjectMapperHolder.readTree(responseBody);
        if (jsonNode.path("timed_out").asBoolean()) {
            throw new RuntimeException("Request to Elasticsearch timed out");
        }
        return jsonNode.path("aggregations").path("total_amount").path("value").asDouble();
    }


}
