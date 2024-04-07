package kz.greetgo.sandboxserver.model.mongo;

import kz.greetgo.sandboxserver.model.web.enums.Gender;
import kz.greetgo.sandboxserver.model.web.read.ClientToRead;
import kz.greetgo.sandboxserver.model.web.upsert.Charm;
import kz.greetgo.sandboxserver.model.web.upsert.ClientAccount;
import kz.greetgo.sandboxserver.model.web.upsert.ClientAddress;
import kz.greetgo.sandboxserver.model.web.upsert.ClientToUpsert;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.List;

@FieldNameConstants
public class ClientDto {
    public ObjectId id;
    public String surname;
    public String name;
    public String patronymic;
    public Gender gender;
    public LocalDate birth_date;
    public Charm charm;
    public ClientAccount account;
    public String homePhone;
    public String workPhone;
    public String mobilePhone;
    public List<ClientAddress> addresses;
    public List<String> phones;
    public List<AccountDto> accounts;

    public String rndTestingId;

    public static ClientDto from(ObjectId id, ClientToUpsert client) {
        ClientDto dto = new ClientDto();

        dto.id = id;
        dto.account = client.getAccount();
        dto.addresses = client.getAddresses();
        dto.birth_date = client.getBirth_date();
        dto.charm = client.getCharm();
        dto.gender = client.getGender();
        dto.name = client.getName();
        dto.surname = client.getSurname();
        dto.patronymic = client.getPatronymic();
        dto.phones = client.getPhones();
        dto.homePhone = client.getHomePhone();
        dto.workPhone = client.getWorkPhone();
        dto.mobilePhone = client.getMobilePhone();
        dto.rndTestingId = client.getRndTestingId();
        return dto;
    }
    public ClientToRead toRead() {
        ClientToRead toRead = new ClientToRead();

        toRead.id = strId();
        toRead.name = name;
        toRead.surname = surname;
        toRead.patronymic = patronymic;
        toRead.gender = gender;
        toRead.birth_date = birth_date;
        toRead.charm = charm;
        toRead.addresses = addresses;
        toRead.phones = phones;
        toRead.account = account;
        toRead.mobilePhone = mobilePhone;
        toRead.workPhone = workPhone;
        toRead.homePhone = homePhone;
        return toRead;
    }

    public String strId() {
        return id.toString();
    }

}
