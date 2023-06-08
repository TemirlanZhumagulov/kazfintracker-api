package kz.greetgo.sandboxserver.config;

import kz.greetgo.sandboxserver.BeanConfigAll;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@ComponentScan(basePackages = "kz.greetgo.sandboxserver.config.impl")
@Import({BeanConfigAll.class})
public class BeanConfigForTests {
}
