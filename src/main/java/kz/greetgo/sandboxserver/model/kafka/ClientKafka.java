package kz.greetgo.sandboxserver.model.kafka;

import kz.greetgo.sandboxserver.model.elastic.ClientElastic;
import kz.greetgo.sandboxserver.model.mongo.ClientDto;
import kz.greetgo.sandboxserver.model.web.enums.Gender;
import kz.greetgo.sandboxserver.model.web.upsert.Charm;
import kz.greetgo.sandboxserver.model.web.upsert.ClientAccount;
import kz.greetgo.sandboxserver.model.web.upsert.ClientAddress;
import kz.greetgo.sandboxserver.model.web.upsert.ClientPhone;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;


public class ClientKafka {
    public String id;
    public ChangeVariant changeVariant;
    public String surname;
    public String name;
    public String patronymic;
    public Gender gender;
    public LocalDate birth_date;
    public Charm charm;
    public List<ClientAddress> addresses;
    public List<ClientPhone> phones;
    public ClientAccount account;

    public static ClientKafka fromDto(ClientDto dto, ChangeVariant changeVariant) {
        ClientKafka kafka = new ClientKafka();
        kafka.id = dto.strId();
        kafka.changeVariant = changeVariant;
        kafka.name = dto.name;
        kafka.surname = dto.surname;
        kafka.patronymic = dto.patronymic;
        kafka.gender = dto.gender;
        kafka.birth_date = dto.birth_date;
        kafka.charm = dto.charm;
        kafka.account = dto.account;
        kafka.addresses = dto.addresses;
        kafka.phones = dto.phones;
        return kafka;
    }

    public ClientElastic toElastic() {
        ClientElastic elastic = new ClientElastic();

        elastic.id = id;
        elastic.full_name = surname + " " + name + " " + patronymic;
        elastic.age = String.valueOf(Period.between(birth_date, LocalDate.now()).getYears());
        elastic.charm = charm.getName();
        elastic.total_balance = Float.toString(account.getMoney());
        elastic.min_balance = Float.toString(account.getMinimumBalance());
        elastic.max_balance = Float.toString(account.getMaximumBalance());
        return elastic;
    }

}
