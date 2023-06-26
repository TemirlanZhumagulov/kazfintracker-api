package kz.greetgo.sandboxserver;

import kz.greetgo.sandboxserver.elastic.ElasticCreator;
import kz.greetgo.sandboxserver.spring_config.connection.Connections;
import kz.greetgo.sandboxserver.spring_config.scheduler.SchedulerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PostConstruct;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@SpringBootApplication
public class SandboxServerApplication extends SpringBootServletInitializer {

    @Lazy
    @Autowired
    private SchedulerManager schedulerManager;

    @Lazy
    @Autowired
    private ElasticCreator elasticCreator;

    @Lazy
    @Autowired
    private Connections connections;

    public static void main(String[] args) {
        SpringApplication.run(SandboxServerApplication.class, args);
    }

    @PostConstruct
    public void initializeOrFinish() {

        connections.waitForAll();

        elasticCreator.createNeededIndexes();

        schedulerManager.start();
    }

}
