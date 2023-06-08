package kz.greetgo.sandboxserver.controller;

import kz.greetgo.sandboxserver.model.web.read.TestModelAToRead;
import kz.greetgo.sandboxserver.model.web.upsert.TestModelAToUpsert;
import kz.greetgo.sandboxserver.register.TestARegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/a/crud")
@CrossOrigin("*")
public class TestACrudController {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private TestARegister testARegister;

  @GetMapping("/load")
  public TestModelAToRead load(@RequestParam("id") String id) {
    return testARegister.load(id);
  }

  @PostMapping("/create")
  public String create(@RequestBody TestModelAToUpsert testModel) {
    return testARegister.create(testModel);
  }

  @PostMapping("/update")
  public void update(@RequestBody TestModelAToUpsert testModel) {
    testARegister.update(testModel);
  }

  @PostMapping("/delete")
  public void delete(@RequestParam("id") String id) {
    testARegister.delete(id);
  }

}
