package kz.kazfintracker.sandboxserver.register;

import kz.kazfintracker.sandboxserver.elastic.model.ClientResponse;
import kz.kazfintracker.sandboxserver.model.web.Paging;
import kz.kazfintracker.sandboxserver.model.elastic.ClientElastic;
import kz.kazfintracker.sandboxserver.model.web.ClientsTableRequest;

public interface ClientElasticRegister {

    ClientResponse loadAll(Paging paging);

    ClientResponse load(ClientsTableRequest tableRequest, Paging paging);

    void create(ClientElastic client);

    void update(ClientElastic client);

    void delete(String id);

    int getClientListCount();

}
