package kz.greetgo.sandboxserver.model.web;

import kz.greetgo.sandboxserver.model.elastic.ClientElastic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientsTableRequest {


    public String full_name;
    public String charm;
    public String age;
    public String total_balance;
    public String min_balance;
    public String max_balance;
    public HashMap<String, Boolean> sorting;
//    public boolean nameSorted;
//    public boolean patronymicSorted;
//    public boolean birth_dateSorted;
//    public boolean total_balanceSorted;
//    public boolean min_balanceSorted;
//    public boolean max_balanceSorted;


    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();

        if (full_name != null) {
            map.put(ClientElastic.Fields.full_name, full_name);
        }
        if (age != null) {
            map.put(ClientElastic.Fields.age, age);
        }
        if (charm != null) {
            map.put(ClientElastic.Fields.charm, charm);
        }
        if (total_balance != null) {
            map.put(ClientElastic.Fields.total_balance, total_balance);
        }
        if (max_balance != null) {
            map.put(ClientElastic.Fields.max_balance, max_balance);
        }
        if (min_balance != null) {
            map.put(ClientElastic.Fields.min_balance, min_balance);
        }

        return map;
    }


}
