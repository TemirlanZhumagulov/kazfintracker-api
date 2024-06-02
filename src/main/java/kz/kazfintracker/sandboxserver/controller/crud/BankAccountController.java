package kz.kazfintracker.sandboxserver.controller.crud;

import kz.kazfintracker.sandboxserver.model.web.BankAccount;
import kz.kazfintracker.sandboxserver.register.CrudRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/crud/bank_account")
@CrossOrigin("*")
public class BankAccountController {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private CrudRegister<BankAccount, Integer> bankAccountRegister;

  @GetMapping("/load")
  public BankAccount load(@RequestParam("id") Integer id) {
    return bankAccountRegister.load(id);
  }

  @PostMapping("/create")
  public void create(@RequestBody BankAccount bankAccount) {
    bankAccountRegister.create(bankAccount);
  }

  @PostMapping("/update")
  public void update(@RequestBody BankAccount bankAccount) {
    bankAccountRegister.update(bankAccount);
  }

  @PostMapping("/delete")
  public void delete(@RequestParam("id") Integer id) {
    bankAccountRegister.delete(id);
  }

}
