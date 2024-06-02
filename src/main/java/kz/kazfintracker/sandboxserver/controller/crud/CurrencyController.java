package kz.kazfintracker.sandboxserver.controller.crud;

import kz.kazfintracker.sandboxserver.model.web.Currency;
import kz.kazfintracker.sandboxserver.register.CrudRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/crud/currency")
@CrossOrigin("*")
public class CurrencyController {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private CrudRegister<Currency, Integer> currencyRegister;

  @GetMapping("/load")
  public Currency load(@RequestParam("id") Integer id) {
    return currencyRegister.load(id);
  }

  @PostMapping("/create")
  public void create(@RequestBody Currency currency) {
    currencyRegister.create(currency);
  }

  @PostMapping("/update")
  public void update(@RequestBody Currency currency) {
    currencyRegister.update(currency);
  }

  @PostMapping("/delete")
  public void delete(@RequestParam("id") Integer id) {
    currencyRegister.delete(id);
  }

}
