package kz.greetgo.sandboxserver.config.impl;

import kz.greetgo.sandboxserver.config.ElasticConfig;
import org.springframework.stereotype.Component;

@Component
public class ElasticConfigForTests implements ElasticConfig {

    @Override
    public boolean updateImmediately() {
        return true;
    }

}
