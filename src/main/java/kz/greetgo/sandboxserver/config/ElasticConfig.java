package kz.greetgo.sandboxserver.config;

import kz.greetgo.conf.hot.DefaultBoolValue;
import kz.greetgo.conf.hot.Description;

@Description("Elastic config")
public interface ElasticConfig {

    @Description("Elastic indexes update policy: if true, then every update will be waited")
    @DefaultBoolValue(false)
    boolean updateImmediately();

}
