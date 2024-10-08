package kz.kazfintracker.sandboxserver.register;

import kz.kazfintracker.sandboxserver.model.web.Paging;
import kz.kazfintracker.sandboxserver.model.web.TableRequest;

import java.util.List;

public interface CrudElasticRegister<T, K> {

  List<T> loadAll(Paging paging);

  List<T> load(TableRequest tableRequest, Paging paging);

  int count();

}
