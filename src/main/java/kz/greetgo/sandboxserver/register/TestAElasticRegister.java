package kz.greetgo.sandboxserver.register;

import kz.greetgo.sandboxserver.model.Paging;
import kz.greetgo.sandboxserver.model.elastic.TestModelAElastic;
import kz.greetgo.sandboxserver.model.web.TableRequest;

import java.util.List;

public interface TestAElasticRegister {

  List<TestModelAElastic> loadAll(Paging paging);

  List<TestModelAElastic> load(TableRequest tableRequest, Paging paging);

  void create(TestModelAElastic modelA);

  void update(TestModelAElastic modelA);

  void delete(String id);

}
