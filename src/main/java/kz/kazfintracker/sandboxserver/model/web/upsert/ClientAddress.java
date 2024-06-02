package kz.kazfintracker.sandboxserver.model.web.upsert;

import kz.kazfintracker.sandboxserver.model.web.enums.AddrType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientAddress {
    private AddrType type;
    private String street;
    private String house;
    private String flat;
}