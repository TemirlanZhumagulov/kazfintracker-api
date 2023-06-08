package kz.greetgo.sandboxserver;

import kz.greetgo.sandboxserver.model.web.upsert.TestModelAToUpsert;
import kz.greetgo.sandboxserver.config.BeanConfigForTests;
import kz.greetgo.sandboxserver.util.IdGenerator;
import kz.greetgo.util.RND;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

@ContextConfiguration(classes = BeanConfigForTests.class)
@TestPropertySource(locations = "classpath:application.properties")
public class ParentTestNG extends AbstractTestNGSpringContextTests {

  protected TestModelAToUpsert rndAToUpsert() {
    TestModelAToUpsert toUpsert = new TestModelAToUpsert();

    toUpsert.id = IdGenerator.generate().toString();
    toUpsert.strField = RND.strEng(10);
    toUpsert.boolField = RND.bool();
    toUpsert.intField = RND.plusInt(100) + 10;

    return toUpsert;
  }

}
