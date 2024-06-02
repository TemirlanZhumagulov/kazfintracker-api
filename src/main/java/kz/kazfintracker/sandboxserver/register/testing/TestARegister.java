package kz.kazfintracker.sandboxserver.register.testing;

import kz.kazfintracker.sandboxserver.model.web.read.TestModelAToRead;
import kz.kazfintracker.sandboxserver.model.web.upsert.TestModelAToUpsert;

public interface TestARegister {

  TestModelAToRead load(String id);

  String create(TestModelAToUpsert testModel);

  void update(TestModelAToUpsert testModel);

  void delete(String id);

}
