package kz.kazfintracker.sandboxserver.controller.crud;

import kz.kazfintracker.sandboxserver.model.web.CategoryTransaction;
import kz.kazfintracker.sandboxserver.register.CrudRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/crud/categoryTransaction")
@CrossOrigin("*")
public class CategoryTransactionController {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private CrudRegister<CategoryTransaction, Integer> categoryTransactionRegister;

  @GetMapping("/load")
  public CategoryTransaction load(@RequestParam("id") Integer id) {
    return categoryTransactionRegister.load(id);
  }

  @PostMapping("/create")
  public void create(@RequestBody CategoryTransaction categoryTransaction) {
    categoryTransactionRegister.create(categoryTransaction);
  }

  @PostMapping("/update")
  public void update(@RequestBody CategoryTransaction categoryTransaction) {
    categoryTransactionRegister.update(categoryTransaction);
  }

  @PostMapping("/delete")
  public void delete(@RequestParam("id") Integer id) {
    categoryTransactionRegister.delete(id);
  }

}

