package kz.kazfintracker.sandboxserver.impl.table;

import kz.kazfintracker.sandboxserver.model.web.BankAccount;
import kz.kazfintracker.sandboxserver.model.web.Paging;
import kz.kazfintracker.sandboxserver.model.web.TableRequest;
import kz.kazfintracker.sandboxserver.register.CrudElasticRegister;

import java.util.List;

public class BankAccountElasticRegisterImpl implements CrudElasticRegister<BankAccount, Integer> {

  @Override
  public List<BankAccount> loadAll(Paging paging) {
    return List.of();
  }

  @Override
  public List<BankAccount> load(TableRequest tableRequest, Paging paging) {
    return List.of();
  }

  @Override
  public void create(BankAccount client) {

  }

  @Override
  public void update(BankAccount client) {

  }

  @Override
  public void delete(Integer id) {

  }

  @Override
  public int count() {
    return 0;
  }

}
