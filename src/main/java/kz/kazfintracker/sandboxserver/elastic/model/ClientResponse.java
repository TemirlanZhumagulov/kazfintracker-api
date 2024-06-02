package kz.kazfintracker.sandboxserver.elastic.model;

import kz.kazfintracker.sandboxserver.model.elastic.ClientElastic;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class ClientResponse {
    private List<ClientElastic> clients;
    private int total;
}
