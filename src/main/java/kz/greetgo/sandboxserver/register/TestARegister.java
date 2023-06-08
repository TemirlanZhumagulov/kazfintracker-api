package kz.greetgo.sandboxserver.register;

import kz.greetgo.sandboxserver.model.web.read.TestModelAToRead;
import kz.greetgo.sandboxserver.model.web.upsert.TestModelAToUpsert;

public interface TestARegister {

  TestModelAToRead load(String id);

  String create(TestModelAToUpsert testModel);

  void update(TestModelAToUpsert testModel);

  void delete(String id);

}
