package kz.kazfintracker.sandboxserver.config.impl;

import kz.kazfintracker.sandboxserver.config.ElasticConfig;
import org.springframework.stereotype.Component;

@Component
public class ElasticConfigForTests implements ElasticConfig {

    @Override
    public boolean updateImmediately() {
        return true;
    }

}
