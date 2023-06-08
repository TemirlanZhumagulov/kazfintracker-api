package kz.greetgo.sandboxserver;

import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
  "kz.greetgo.sandboxserver.elastic",
  "kz.greetgo.sandboxserver.impl",
  "kz.greetgo.sandboxserver.kafka",
  "kz.greetgo.sandboxserver.mongo",
  "kz.greetgo.sandboxserver.spring_config",
  "kz.greetgo.sandboxserver.scheduler",
})
public class BeanConfigAll {
}
