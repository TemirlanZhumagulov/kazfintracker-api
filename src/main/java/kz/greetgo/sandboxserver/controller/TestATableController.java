package kz.greetgo.sandboxserver.controller;

import java.util.List;
import kz.greetgo.sandboxserver.model.Paging;
import kz.greetgo.sandboxserver.model.elastic.TestModelAElastic;
import kz.greetgo.sandboxserver.model.web.TableRequest;
import kz.greetgo.sandboxserver.register.TestAElasticRegister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/a/table")
@CrossOrigin("*")
public class TestATableController {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private TestAElasticRegister testAElasticRegister;

  @GetMapping("/all")
  public List<TestModelAElastic> loadAll(
    @RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
    @RequestParam(value = "limit", required = false, defaultValue = "10") int limit) {
    return testAElasticRegister.loadAll(Paging.of(offset, limit));
  }

  @PostMapping("/filtered")
  public List<TestModelAElastic> load(@RequestParam(value = "offset", required = false, defaultValue = "0") int offset,
                                      @RequestParam(value = "limit", required = false, defaultValue = "10") int limit,
                                      @RequestBody TableRequest tableRequest) {
    return testAElasticRegister.load(tableRequest, Paging.of(offset, limit));
  }

}
