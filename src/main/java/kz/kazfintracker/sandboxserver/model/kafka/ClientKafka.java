package kz.kazfintracker.sandboxserver.model.kafka;

import kz.kazfintracker.sandboxserver.model.elastic.ClientElastic;
import kz.kazfintracker.sandboxserver.model.mongo.ClientDto;
import kz.kazfintracker.sandboxserver.model.web.enums.Gender;
import kz.kazfintracker.sandboxserver.model.web.upsert.Charm;
import kz.kazfintracker.sandboxserver.model.web.upsert.ClientAccount;
import kz.kazfintracker.sandboxserver.model.web.upsert.ClientAddress;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;


public class ClientKafka {
    public String id;
    public String surname;
    public String name;
    public String patronymic;
    public Gender gender;
    public LocalDate birth_date;
    public Charm charm;
    public String homePhone;
    public String workPhone;
    public String mobilePhone;
    public List<ClientAddress> addresses;
    public List<String> phones;
    public ClientAccount account;

    public String rndTestingId;

    public static ClientKafka fromDto(ClientDto dto) {
        ClientKafka kafka = new ClientKafka();
        kafka.id = dto.strId();
        kafka.name = dto.name;
        kafka.surname = dto.surname;
        kafka.patronymic = dto.patronymic;
        kafka.gender = dto.gender;
        kafka.birth_date = dto.birth_date;
        kafka.charm = dto.charm;
        kafka.account = dto.account;
        kafka.addresses = dto.addresses;
        kafka.mobilePhone = dto.mobilePhone;
        kafka.homePhone = dto.homePhone;
        kafka.workPhone = dto.workPhone;
        kafka.phones = dto.phones;
        kafka.rndTestingId = dto.rndTestingId;
        return kafka;
    }

    public ClientElastic toElastic() {
        ClientElastic elastic = new ClientElastic();

        elastic.id = id;
        elastic.full_name = surname + " " + name + " " + patronymic;
        elastic.age = String.valueOf(Period.between(birth_date, LocalDate.now()).getYears());
        elastic.charm = charm.getName();
        elastic.total_balance = Float.toString(account.getTotal_balance());
        elastic.min_balance = Float.toString(account.getMin_balance());
        elastic.max_balance = Float.toString(account.getMax_balance());
        elastic.rndTestingId = rndTestingId;
        return elastic;
    }

}
