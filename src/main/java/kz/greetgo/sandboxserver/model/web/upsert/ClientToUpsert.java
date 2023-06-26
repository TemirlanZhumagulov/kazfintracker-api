package kz.greetgo.sandboxserver.model.web.upsert;

import kz.greetgo.sandboxserver.model.web.enums.Gender;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
public class ClientToUpsert {
    private String id;
    private String surname;
    private String name;
    private String patronymic;
    private Gender gender;
    private LocalDate birth_date;
    private Charm charm;
    private List<ClientAddress> addresses;
    private List<ClientPhone> phones;
    private ClientAccount account;
    public ObjectId objectId() {
        return new ObjectId(id);
    }


}
