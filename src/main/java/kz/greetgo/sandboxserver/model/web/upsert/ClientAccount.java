package kz.greetgo.sandboxserver.model.web.upsert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientAccount {
    private Float total_balance;
    private Float max_balance;
    private Float min_balance;

}
