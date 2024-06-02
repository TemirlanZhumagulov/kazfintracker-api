package kz.kazfintracker.sandboxserver.config;

import kz.kazfintracker.sandboxserver.BeanConfigAll;
import kz.kazfintracker.sandboxserver.kafka.KafkaConfigForTests;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = "kz.greetgo.sandboxserver.config.impl")
@Import({BeanConfigAll.class, KafkaConfigForTests.class})
public class BeanConfigForTests {
}
