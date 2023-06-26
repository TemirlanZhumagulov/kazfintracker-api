package kz.greetgo.sandboxserver.model.elastic;

import kz.greetgo.sandboxserver.elastic.ElasticIndexes;
import lombok.experimental.FieldNameConstants;

import java.util.Date;
import java.util.Map;

@FieldNameConstants
public class ClientElastic {
    public String id;
    public String charm;
    public String full_name;
    public String age;
    public String total_balance;
    public String min_balance;
    public String max_balance;

    public static ClientElastic fromMap(Map<String, String> map) {
        ClientElastic client = new ClientElastic();

        client.id = map.get(ClientElastic.Fields.id);
        client.full_name = map.get(Fields.full_name);
        client.charm = map.get(Fields.charm);
        client.age = map.get(Fields.age);
        client.total_balance = map.get(Fields.total_balance);
        client.min_balance = map.get(Fields.min_balance);
        client.max_balance = map.get(Fields.max_balance);
        return client;
    }

    public static String indexName() {

        return ElasticIndexes.INDEX_CLIENT;
    }

    public static String mapping() {
        return "{\n" +
                "  \"mappings\": {\n" +
                "    \"properties\": {\n" +
                "      \"id\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"full_name\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"charm\": {\n" +
                "        \"type\": \"keyword\"\n" +
                "      },\n" +
                "      \"age\": {\n" +
                "        \"type\": \"integer\"\n" +
                "      },\n" +
                "      \"total_balance\": {\n" +
                "        \"type\": \"float\"\n" +
                "      },\n" +
                "      \"min_balance\": {\n" +
                "        \"type\": \"float\"\n" +
                "      },\n" +
                "      \"max_balance\": {\n" +
                "        \"type\": \"float\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }

}
