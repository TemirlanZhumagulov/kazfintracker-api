package kz.greetgo.sandboxserver.model.web.read;

import kz.greetgo.sandboxserver.model.web.enums.Gender;
import kz.greetgo.sandboxserver.model.web.upsert.Charm;
import kz.greetgo.sandboxserver.model.web.upsert.ClientAccount;
import kz.greetgo.sandboxserver.model.web.upsert.ClientAddress;
import kz.greetgo.sandboxserver.model.web.upsert.ClientPhone;

import java.time.LocalDate;
import java.util.List;

public class ClientToRead {
    public String id;
    public String surname;
    public String name;
    public String patronymic;
    public Gender gender;
    public LocalDate birth_date;
    public Charm charm;
    public ClientAccount account;
    public List<ClientAddress> addresses;
    public List<ClientPhone> phones;
}
