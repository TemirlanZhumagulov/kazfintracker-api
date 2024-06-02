package kz.kazfintracker.sandboxserver.model.web.read;

import kz.kazfintracker.sandboxserver.model.web.enums.Gender;
import kz.kazfintracker.sandboxserver.model.web.upsert.Charm;
import kz.kazfintracker.sandboxserver.model.web.upsert.ClientAccount;
import kz.kazfintracker.sandboxserver.model.web.upsert.ClientAddress;

import java.time.LocalDate;
import java.util.List;

public class ClientToRead {
    public String id;
    public String surname;
    public String name;
    public String patronymic;
    public String email;
    public Gender gender;
    public LocalDate birth_date;
    public Charm charm;
    public ClientAccount account;
    public String homePhone;
    public String workPhone;
    public String mobilePhone;
    public List<ClientAddress> addresses;
    public List<String> phones;
}
