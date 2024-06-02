package kz.kazfintracker.sandboxserver.register;

import kz.kazfintracker.sandboxserver.model.web.read.ClientToRead;
import kz.kazfintracker.sandboxserver.model.web.upsert.ClientToUpsert;

public interface ClientRegister {

    ClientToRead load(String id);

    String create(ClientToUpsert client);

    String update(ClientToUpsert client);

    void delete(String id);

}
