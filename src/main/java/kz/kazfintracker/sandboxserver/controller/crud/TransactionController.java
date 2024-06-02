package kz.kazfintracker.sandboxserver.controller.crud;

import kz.kazfintracker.sandboxserver.model.web.Transaction;
import kz.kazfintracker.sandboxserver.register.CrudRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/crud/transaction")
@CrossOrigin("*")
public class TransactionController {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private CrudRegister<Transaction, Integer> transactionRegister;

  @GetMapping("/load")
  public Transaction load(@RequestParam("id") Integer id) {
    return transactionRegister.load(id);
  }

  @PostMapping("/create")
  public void create(@RequestBody Transaction transaction) {
    transactionRegister.create(transaction);
  }

  @PostMapping("/update")
  public void update(@RequestBody Transaction transaction) {
    transactionRegister.update(transaction);
  }

  @PostMapping("/delete")
  public void delete(@RequestParam("id") Integer id) {
    transactionRegister.delete(id);
  }

}
