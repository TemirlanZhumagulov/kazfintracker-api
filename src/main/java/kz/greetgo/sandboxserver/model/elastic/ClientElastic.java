package kz.greetgo.sandboxserver.model.elastic;

import kz.greetgo.sandboxserver.elastic.ElasticIndexes;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

@FieldNameConstants
public class ClientElastic {
    public String id;
    public String charm;
    public String full_name;
    public String age;
    public String total_balance;
    public String min_balance;
    public String max_balance;

    public String rndTestingId;
    public static ClientElastic fromMap(Map<String, String> map) {
        ClientElastic client = new ClientElastic();

        client.id = map.get(ClientElastic.Fields.id);
        client.full_name = map.get(Fields.full_name);
        client.charm = map.get(Fields.charm);
        client.age = map.get(Fields.age);
        client.total_balance = map.get(Fields.total_balance);
        client.min_balance = map.get(Fields.min_balance);
        client.max_balance = map.get(Fields.max_balance);
        client.rndTestingId = map.get(Fields.rndTestingId);
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
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
                "      },\n" +
                "      \"charm\": {\n" +
                "        \"type\": \"text\",\n" +
                "        \"fields\": {\n" +
                "          \"keyword\": {\n" +
                "            \"type\": \"keyword\"\n" +
                "          }\n" +
                "        }\n" +
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
                "      },\n" +
                "      \"rndTestingId\": {\n" +
                "        \"type\": \"text\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientElastic that = (ClientElastic) o;
        return Objects.equals(id, that.id) && Objects.equals(charm, that.charm) && Objects.equals(full_name, that.full_name) && Objects.equals(age, that.age) && Objects.equals(total_balance, that.total_balance) && Objects.equals(min_balance, that.min_balance) && Objects.equals(max_balance, that.max_balance) && Objects.equals(rndTestingId, that.rndTestingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, charm, full_name, age, total_balance, min_balance, max_balance, rndTestingId);
    }
}
