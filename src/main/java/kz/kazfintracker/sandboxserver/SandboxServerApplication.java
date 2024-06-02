package kz.kazfintracker.sandboxserver;

import kz.kazfintracker.sandboxserver.elastic.ElasticCreator;
import kz.kazfintracker.sandboxserver.impl.crud.ClientRegisterImpl;
import kz.kazfintracker.sandboxserver.model.web.enums.AddrType;
import kz.kazfintracker.sandboxserver.model.web.enums.Gender;
import kz.kazfintracker.sandboxserver.model.web.upsert.Charm;
import kz.kazfintracker.sandboxserver.model.web.upsert.ClientAccount;
import kz.kazfintracker.sandboxserver.model.web.upsert.ClientAddress;
import kz.kazfintracker.sandboxserver.model.web.upsert.ClientToUpsert;
import kz.kazfintracker.sandboxserver.spring_config.connection.Connections;
import kz.kazfintracker.sandboxserver.spring_config.scheduler.SchedulerManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@SpringBootApplication
public class SandboxServerApplication extends SpringBootServletInitializer {

  @Lazy
  @Autowired
  private SchedulerManager schedulerManager;

  @Lazy
  @Autowired
  private ElasticCreator elasticCreator;

  @Lazy
  @Autowired
  private Connections connections;

  @Autowired
  private ClientRegisterImpl clientRegister;

  private final Random random = new Random();

  public static void main(String[] args) {
    SpringApplication.run(SandboxServerApplication.class, args);
  }

  @PostConstruct
  public void initializeOrFinish() {

    connections.waitForAll();

    elasticCreator.createNeededIndexes();

    schedulerManager.start();
  }

// implements CommandLineRunner
//    @Override
//    public void run(String... args) throws InterruptedException {
//        Thread.sleep(3000L); // Wait before elastic is created
//        List<ClientToUpsert> clients = generateRandomClients();
//        for (ClientToUpsert client : clients) {
//            clientRegister.create(client);
//        }
//    }

  private List<ClientToUpsert> generateRandomClients() {
    List<ClientToUpsert> clients = new ArrayList<>();

    for (int i = 0; i < 100; i++) {
      ClientToUpsert client = new ClientToUpsert();
      client.setId("1");
      client.setSurname(generateRandomString(10));
      client.setName(generateRandomString(10));
      client.setPatronymic(generateRandomString(10));
      client.setGender(random.nextBoolean() ? Gender.MALE : Gender.FEMALE);
      client.setBirth_date(generateRandomDate());
      client.setCharm(generateRandomCharm());
      client.setAddresses(generateRandomAddresses());
      client.setHomePhone(generateRandomDigits());
      client.setWorkPhone(generateRandomDigits());
      client.setMobilePhone(generateRandomDigits());
      client.setPhones(generateRandomPhones());
      client.setAccount(generateRandomAccount());
      clients.add(client);
    }

    return clients;
  }

  private String generateRandomString(int length) {
    String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      int randomIndex = random.nextInt(chars.length());
      sb.append(chars.charAt(randomIndex));
    }

    return sb.toString();
  }

  private LocalDate generateRandomDate() {
    LocalDate minDate = LocalDate.of(1901, 1, 1);
    LocalDate maxDate = LocalDate.now().minus(Period.ofYears(18)); // Adjust to allow clients who are 18 or older
    long minDay = minDate.toEpochDay();
    long maxDay = maxDate.toEpochDay();
    long randomDay = minDay + random.nextInt((int) (maxDay - minDay));

    return LocalDate.ofEpochDay(randomDay);
  }

  private Charm generateRandomCharm() {
    Charm charm = new Charm();
    charm.setName(generateRandomString(10));
    charm.setDescription(generateRandomString(20));
    charm.setEnergy(random.nextFloat());

    return charm;
  }

  private List<ClientAddress> generateRandomAddresses() {
    List<ClientAddress> addresses = new ArrayList<>();

    for (int i = 0; i < 2; i++) {
      ClientAddress address = new ClientAddress();
      address.setType(AddrType.values()[random.nextInt(AddrType.values().length)]);
      address.setStreet(generateRandomString(10));
      address.setHouse(generateRandomString(5));
      address.setFlat(generateRandomString(3));

      addresses.add(address);
    }

    return addresses;
  }

  private List<String> generateRandomPhones() {
    List<String> phones = new ArrayList<>();

    for (int i = 0; i < 3; i++) {
      phones.add(generateRandomDigits());
    }

    return phones;
  }

  private String generateRandomDigits() {
    return "22222222222";
  }

  private ClientAccount generateRandomAccount() {
    ClientAccount account = new ClientAccount();
    account.setTotal_balance(random.nextFloat());
    account.setMax_balance(random.nextFloat());
    account.setMin_balance(random.nextFloat());

    return account;
  }

}
