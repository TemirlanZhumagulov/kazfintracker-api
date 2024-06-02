package kz.kazfintracker.sandboxserver.controller.crud;

import kz.kazfintracker.sandboxserver.model.web.RecurringTransactionAmount;
import kz.kazfintracker.sandboxserver.register.CrudRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/crud/recur")
@CrossOrigin("*")
public class RecurringTransactionAmountController {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private CrudRegister<RecurringTransactionAmount, Integer> rtaRegister;

  @GetMapping("/load")
  public RecurringTransactionAmount load(@RequestParam("id") Integer id) {
    return rtaRegister.load(id);
  }

  @PostMapping("/create")
  public void create(@RequestBody RecurringTransactionAmount bankAccount) {
    rtaRegister.create(bankAccount);
  }

  @PostMapping("/update")
  public void update(@RequestBody RecurringTransactionAmount bankAccount) {
    rtaRegister.update(bankAccount);
  }

  @PostMapping("/delete")
  public void delete(@RequestParam("id") Integer id) {
    rtaRegister.delete(id);
  }

}
