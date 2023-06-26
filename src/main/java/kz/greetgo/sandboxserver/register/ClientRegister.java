package kz.greetgo.sandboxserver.register;

import kz.greetgo.sandboxserver.model.web.read.ClientToRead;
import kz.greetgo.sandboxserver.model.web.upsert.ClientToUpsert;

public interface ClientRegister {
    ClientToRead load(String id);

    String create(ClientToUpsert client);

    void update(ClientToUpsert client);

    void delete(String id);
}
