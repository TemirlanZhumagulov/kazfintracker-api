package kz.greetgo.sandboxserver.impl;

import com.mongodb.client.model.Filters;
import kz.greetgo.sandboxserver.ParentTestNG;
import kz.greetgo.sandboxserver.exception.NoElementWasFoundException;
import kz.greetgo.sandboxserver.model.mongo.ClientDto;
import kz.greetgo.sandboxserver.model.web.enums.AddrType;
import kz.greetgo.sandboxserver.model.web.enums.Gender;
import kz.greetgo.sandboxserver.model.web.read.ClientToRead;
import kz.greetgo.sandboxserver.model.web.upsert.Charm;
import kz.greetgo.sandboxserver.model.web.upsert.ClientAccount;
import kz.greetgo.sandboxserver.model.web.upsert.ClientAddress;
import kz.greetgo.sandboxserver.model.web.upsert.ClientToUpsert;
import kz.greetgo.sandboxserver.mongo.MongoAccess;
import kz.greetgo.sandboxserver.register.ClientRegister;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientRegisterImplTest extends ParentTestNG {

  @Autowired
  private MongoAccess mongoAccess;

  @Autowired
  private ClientRegister clientRegister;


  @Test
  public void createClient() {
    ClientToUpsert toUpsert = clientToUpsert();

    //
    //
    String id = clientRegister.create(toUpsert);
    //
    //

    assertThat(id).isNotNull();

    ClientDto dto = mongoAccess.client().find(Filters.eq("_id", new ObjectId(id))).first();

    assertThat(dto).isNotNull();

    assertThat(dto.name).isEqualTo(toUpsert.getName());
    assertThat(dto.surname).isEqualTo(toUpsert.getSurname());
    assertThat(dto.patronymic).isEqualTo(toUpsert.getPatronymic());
    assertThat(dto.gender).isEqualTo(toUpsert.getGender());
    assertThat(dto.birth_date).isEqualTo(toUpsert.getBirth_date());
    assertThat(dto.charm).isEqualTo(toUpsert.getCharm());
    assertThat(dto.addresses).isEqualTo(toUpsert.getAddresses());
    assertThat(dto.phones).isEqualTo(toUpsert.getPhones());
    assertThat(dto.account).isEqualTo(toUpsert.getAccount());


  }
  //
  // Testing Load
  //
  @Test
  public void loadClient(){
    ClientToUpsert toUpsert = clientToUpsert();

    //
    //
    String id = clientRegister.create(toUpsert);
    //
    //

    //
    //
    ClientToRead toRead = clientRegister.load(id);
    //
    //

    assertThat(id).isEqualTo(toRead.id);
    assertThat(toUpsert.getName()).isEqualTo(toRead.name);
    assertThat(toUpsert.getSurname()).isEqualTo(toRead.surname);
    assertThat(toUpsert.getPatronymic()).isEqualTo(toRead.patronymic);
    assertThat(toUpsert.getBirth_date()).isEqualTo(toRead.birth_date);
    assertThat(toUpsert.getGender()).isEqualTo(toRead.gender);
    assertThat(toUpsert.getPhones()).isEqualTo(toRead.phones);
    assertThat(toUpsert.getCharm()).isEqualTo(toRead.charm);
    assertThat(toUpsert.getAccount()).isEqualTo(toRead.account);
    assertThat(toUpsert.getAddresses()).isEqualTo(toRead.addresses);
  }
  //
  // Testing Update
  //
  @Test
  public void updateClient(){
    ClientToUpsert toUpsert = clientToUpsert();
    //
    //
    String id = clientRegister.create(toUpsert);
    //
    //
    toUpsert.setId(id);
    toUpsert.setName("Karina");
    toUpsert.setSurname("Buasheva");
    toUpsert.setPatronymic("Aidoskizi");
    toUpsert.setGender(Gender.FEMALE);
    toUpsert.setCharm(new Charm("Extra Holeric", "Energetic",100F));
    toUpsert.setAddresses(List.of(
            new ClientAddress(AddrType.REG, "asd", "123", "8"),
            new ClientAddress(AddrType.FAC, "asdaads", "221", "8")
    ));
    toUpsert.setPhones(List.of("8888888888888", "8888888888888"));
    toUpsert.setAccount(new ClientAccount(1F, 1F, 10000000F));
    toUpsert.setBirth_date(LocalDate.of(2002,12, 3));

    //
    //
    clientRegister.update(toUpsert);
    //
    //

    //
    //
    ClientToRead toRead = clientRegister.load(id);
    //
    //

    assertThat(id).isEqualTo(toRead.id);
    assertThat(toUpsert.getName()).isEqualTo(toRead.name);
    assertThat(toUpsert.getSurname()).isEqualTo(toRead.surname);
    assertThat(toUpsert.getPatronymic()).isEqualTo(toRead.patronymic);
    assertThat(toUpsert.getBirth_date()).isEqualTo(toRead.birth_date);
    assertThat(toUpsert.getGender()).isEqualTo(toRead.gender);
    assertThat(toUpsert.getPhones()).isEqualTo(toRead.phones);
    assertThat(toUpsert.getCharm()).isEqualTo(toRead.charm);
    assertThat(toUpsert.getAccount()).isEqualTo(toRead.account);
    assertThat(toUpsert.getAddresses()).isEqualTo(toRead.addresses);

  }
  //
  // Testing Delete
  //
  @Test(expectedExceptions = NoElementWasFoundException.class, expectedExceptionsMessageRegExp = "^Dto with ID .* does not exist$")
  public void deleteClient(){
    ClientToUpsert toUpsert = clientToUpsert();

    //
    //
    String id = clientRegister.create(toUpsert);
    //
    //
    assertThat(id).isNotNull();
    System.out.println(clientRegister.load(id));

    //
    //
    clientRegister.delete(id);
    //
    //

    //
    //
    System.out.println(clientRegister.load(id));
    //
    //
  }
}

