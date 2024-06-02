package kz.kazfintracker.sandboxserver;

import kz.kazfintracker.sandboxserver.config.BeanConfigForTests;
import kz.kazfintracker.sandboxserver.kafka.KafkaProducerSimulator;
import kz.kazfintracker.sandboxserver.model.web.enums.AddrType;
import kz.kazfintracker.sandboxserver.model.web.enums.Gender;
import kz.kazfintracker.sandboxserver.util.Ids;
import kz.greetgo.util.RND;
import kz.kazfintracker.sandboxserver.model.web.upsert.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ContextConfiguration(classes = BeanConfigForTests.class)
@TestPropertySource(locations = "classpath:application.properties")
public class ParentTestNG extends AbstractTestNGSpringContextTests {

  @Autowired
  protected KafkaProducerSimulator kafkaProducerSimulator;

  @BeforeMethod
  public void init() {
    kafkaProducerSimulator.clear();
  }

  protected TestModelAToUpsert rndAToUpsert() {
    TestModelAToUpsert toUpsert = new TestModelAToUpsert();

    toUpsert.id = Ids.generate().toString();
    toUpsert.strField = RND.strEng(10);
    toUpsert.boolField = RND.bool();
    toUpsert.intField = RND.plusInt(100) + 10;

    return toUpsert;
  }


  protected ClientToUpsert clientToUpsert(){
    ClientToUpsert toUpsert = new ClientToUpsert();
    toUpsert.setId(Ids.generate().toString());
    toUpsert.setName("Temirlan");
    toUpsert.setSurname("Zhumagulov");
    toUpsert.setPatronymic("Asda");
    toUpsert.setCharm(new Charm("Holeric", "asda", 23.32F));
    toUpsert.setAddresses(List.of(
            new ClientAddress(AddrType.REG, "asd", "123", "8")
    ));
    toUpsert.setGender(Gender.MALE);
    toUpsert.setHomePhone("332281301312");
    toUpsert.setWorkPhone("332281301312");
    toUpsert.setMobilePhone("332281301312");
    toUpsert.setBirth_date(LocalDate.of(2002,11, 3));
    toUpsert.setPhones(List.of("12381301312", "12381301312", "12381301312"));
    toUpsert.setAccount(new ClientAccount(123131F, 321F, 123F));
    return toUpsert;
  }
  protected List<ClientToUpsert> getTestClients(String uniqueTestingId){
    List<ClientToUpsert> clients = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      ClientToUpsert toUpsert = new ClientToUpsert();
      toUpsert.setId("1");
      toUpsert.setName(i+ "Temirlan");
      toUpsert.setSurname(i+ "Zhumagulov");
      toUpsert.setPatronymic(i+ "Asda");
      toUpsert.setCharm(new Charm("Holeric" + i, "asda", 23.32F));
      toUpsert.setAddresses(List.of(
              new ClientAddress(AddrType.REG, "asd", "123", "8")
      ));
      toUpsert.setGender(Gender.MALE);
      toUpsert.setBirth_date(LocalDate.of(2000+i,11, 3 ));
      toUpsert.setHomePhone("332281301312");
      toUpsert.setWorkPhone("332281301312");
      toUpsert.setMobilePhone("332281301312");      toUpsert.setPhones(List.of("12381301312", "12381301312", "12381301312"));
      toUpsert.setAccount(new ClientAccount(1234F + i *10, 11F + i * 10, 11F + i * 10));
      toUpsert.setRndTestingId(uniqueTestingId);
      clients.add(toUpsert);
    }
    return clients;
  }
  protected List<ClientToUpsert> getTestClientsForFilter(String uniqueName){
    List<ClientToUpsert> clients = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      ClientToUpsert toUpsert = new ClientToUpsert();
      toUpsert.setId("1");
      if(i < 3){
        toUpsert.setName(uniqueName);
        toUpsert.setSurname(uniqueName);
        toUpsert.setPatronymic(uniqueName);
      } else {
        toUpsert.setName(i+ "Temirlan");
        toUpsert.setSurname(i+ "Zhumagulov");
        toUpsert.setPatronymic(i+ "Asda");
      }
      toUpsert.setCharm(new Charm("Holeric" + i, "asda", 23.32F));
      toUpsert.setAddresses(List.of(
              new ClientAddress(AddrType.REG, "asd", "123", "8")
      ));
      toUpsert.setGender(Gender.MALE);
      toUpsert.setBirth_date(LocalDate.of(2000+i,11, 3 ));
      toUpsert.setHomePhone("332281301312");
      toUpsert.setWorkPhone("332281301312");
      toUpsert.setMobilePhone("332281301312");      toUpsert.setPhones(List.of("12381301312", "12381301312", "12381301312"));
      toUpsert.setAccount(new ClientAccount(1234F + i *10, 11F + i * 10, 11F + i * 10));
      toUpsert.setRndTestingId("1231");
      clients.add(toUpsert);
    }
    return clients;
  }
}
