package kz.kazfintracker.sandboxserver.controller.crud;

import kz.kazfintracker.sandboxserver.model.web.Budget;
import kz.kazfintracker.sandboxserver.register.CrudRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/crud/budget")
@CrossOrigin("*")
public class BudgetController {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private CrudRegister<Budget, Integer> budgetRegister;

  @GetMapping("/load")
  public Budget load(@RequestParam("id") Integer id) {
    return budgetRegister.load(id);
  }

  @PostMapping("/create")
  public void create(@RequestBody Budget budget) {
    budgetRegister.create(budget);
  }

  @PostMapping("/update")
  public void update(@RequestBody Budget budget) {
    budgetRegister.update(budget);
  }

  @PostMapping("/delete")
  public void delete(@RequestParam("id") Integer id) {
    budgetRegister.delete(id);
  }
}
