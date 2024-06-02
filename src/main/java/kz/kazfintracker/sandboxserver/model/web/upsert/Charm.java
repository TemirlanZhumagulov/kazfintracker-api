package kz.kazfintracker.sandboxserver.model.web.upsert;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Charm {
    private String name;
    private String description;
    private Float energy;
}
