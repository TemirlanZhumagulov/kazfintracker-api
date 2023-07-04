package kz.greetgo.sandboxserver.config;

import kz.greetgo.sandboxserver.BeanConfigAll;
import kz.greetgo.sandboxserver.kafka.KafkaConfigForTests;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = "kz.greetgo.sandboxserver.config.impl")
@Import({BeanConfigAll.class, KafkaConfigForTests.class})
public class BeanConfigForTests {
}
