package kz.kazfintracker.sandboxserver;

import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
  "kz.kazfintracker.sandboxserver.auth",
  "kz.kazfintracker.sandboxserver.elastic",
  "kz.kazfintracker.sandboxserver.impl",
  "kz.kazfintracker.sandboxserver.kafka",
  "kz.kazfintracker.sandboxserver.mongo",
  "kz.kazfintracker.sandboxserver.spring_config",
  "kz.kazfintracker.sandboxserver.scheduler",
})
public class BeanConfigAll {
}
