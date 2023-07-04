package kz.greetgo.sandboxserver.model.web.upsert;

import kz.greetgo.sandboxserver.model.web.enums.PhoneType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientPhone {
    private String number;
}
