package kz.kazfintracker.sandboxserver.register.testing;

import kz.kazfintracker.sandboxserver.model.web.Paging;
import kz.kazfintracker.sandboxserver.model.elastic.TestModelAElastic;
import kz.kazfintracker.sandboxserver.model.web.TestTableRequest;

import java.util.List;

public interface TestAElasticRegister {

  List<TestModelAElastic> loadAll(Paging paging);

  List<TestModelAElastic> load(TestTableRequest testTableRequest, Paging paging);

  void create(TestModelAElastic modelA);

  void update(TestModelAElastic modelA);

  void delete(String id);

}
