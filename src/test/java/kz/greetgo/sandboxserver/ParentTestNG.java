package kz.greetgo.sandboxserver;

import kz.greetgo.sandboxserver.model.web.enums.AddrType;
import kz.greetgo.sandboxserver.model.web.enums.Gender;
import kz.greetgo.sandboxserver.model.web.enums.PhoneType;
import kz.greetgo.sandboxserver.model.web.upsert.*;
import kz.greetgo.sandboxserver.config.BeanConfigForTests;
import kz.greetgo.sandboxserver.util.IdGenerator;
import kz.greetgo.util.RND;
import org.bson.codecs.jsr310.LocalDateCodec;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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


  protected ClientToUpsert clientToUpsert(){
    ClientToUpsert toUpsert = new ClientToUpsert();
    toUpsert.setId(IdGenerator.generate().toString());
    String id = toUpsert.getId();
    toUpsert.setName("Temirlan");
    toUpsert.setSurname("Zhumagulov");
    toUpsert.setPatronymic("Asda");
    toUpsert.setCharm(new Charm("Holeric", "asda", 23.32F));
    toUpsert.setAddresses(List.of(
            new ClientAddress(AddrType.REG, "asd", "123", "8")
    ));
    toUpsert.setGender(Gender.MALE);
    toUpsert.setBirth_date(LocalDate.of(2002,11, 3));
    toUpsert.setPhones(List.of(
            new ClientPhone("12381301312", PhoneType.HOME),
            new ClientPhone("12381301312", PhoneType.WORK),
            new ClientPhone("12381301312", PhoneType.MOBILE)
    ));
    toUpsert.setAccount(new ClientAccount(123131F, 321F, 123F));
    return toUpsert;
  }

}
