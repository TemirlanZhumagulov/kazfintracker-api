package kz.greetgo.sandboxserver.model.web.upsert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientAccount {
    private Float money;
    private Float maximumBalance;
    private Float minimumBalance;

}
