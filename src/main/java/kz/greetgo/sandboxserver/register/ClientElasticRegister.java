package kz.greetgo.sandboxserver.register;

import kz.greetgo.sandboxserver.elastic.model.ClientResponse;
import kz.greetgo.sandboxserver.model.Paging;
import kz.greetgo.sandboxserver.model.elastic.ClientElastic;
import kz.greetgo.sandboxserver.model.web.ClientsTableRequest;

public interface ClientElasticRegister {
    ClientResponse loadAll(Paging paging);

    ClientResponse load(ClientsTableRequest tableRequest, Paging paging);
    void create(ClientElastic client);

    void update(ClientElastic client);

    void delete(String id);

    int getClientListCount();
}
