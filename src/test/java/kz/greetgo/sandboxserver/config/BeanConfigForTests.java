package kz.greetgo.sandboxserver.config;

import kz.greetgo.sandboxserver.BeanConfigAll;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = {
        "kz.greetgo.sandboxserver.elastic",
        "kz.greetgo.sandboxserver.impl",
        "kz.greetgo.sandboxserver.kafka",
        "kz.greetgo.sandboxserver.mongo",
        "kz.greetgo.sandboxserver.spring_config",
        "kz.greetgo.sandboxserver.scheduler",
        "kz.greetgo.sandboxserver.config.impl"})
@Import({BeanConfigAll.class})
public class BeanConfigForTests {
}
