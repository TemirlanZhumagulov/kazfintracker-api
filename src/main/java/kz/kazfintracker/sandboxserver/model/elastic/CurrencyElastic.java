package kz.kazfintracker.sandboxserver.model.elastic;

import lombok.Data;

import java.util.Map;

@Data
public class CurrencyElastic {
    private String id;
    private String symbol;
    private String code;
    private String name;
    private boolean mainCurrency;

    public static CurrencyElastic fromMap(Map<String, String> map) {
        CurrencyElastic currency = new CurrencyElastic();
        currency.setId(map.get("id"));
        currency.setSymbol(map.get("symbol"));
        currency.setCode(map.get("code"));
        currency.setName(map.get("name"));
        currency.setMainCurrency(Boolean.parseBoolean(map.get("mainCurrency")));
        return currency;
    }
}
