package kz.greetgo.sandboxserver.impl;

import com.mongodb.client.model.Filters;
import kz.greetgo.sandboxserver.ParentTestNG;
import kz.greetgo.sandboxserver.kafka.consumer.ModelAElasticConsumer;
import kz.greetgo.sandboxserver.model.Paging;
import kz.greetgo.sandboxserver.model.elastic.TestModelAElastic;
import kz.greetgo.sandboxserver.model.mongo.TestModelADto;
import kz.greetgo.sandboxserver.model.web.TableRequest;
import kz.greetgo.sandboxserver.model.web.upsert.TestModelAToUpsert;
import kz.greetgo.sandboxserver.mongo.MongoAccess;
import kz.greetgo.sandboxserver.register.TestAElasticRegister;
import kz.greetgo.sandboxserver.register.TestARegister;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class TestARegisterImplTest extends ParentTestNG {

    @Autowired
    private TestARegister testARegister;

    @Autowired
    private MongoAccess mongoAccess;

    @Autowired
    private TestAElasticRegister testAElasticRegister;


    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "^testModel cannot be null$")
    public void create__modelIsNull() {

        //
        //
        testARegister.create(null);
        //
        //

    }

    @Test
    public void create() {
        TestModelAToUpsert toUpsert = rndAToUpsert();

        //
        //
        String id = testARegister.create(toUpsert);
        //
        //

        assertThat(id).isNotNull();

        TestModelADto dto = mongoAccess.testModelA().find(Filters.eq("_id", new ObjectId(id))).first();

        assertThat(dto).isNotNull();

        assertThat(dto.strField).isEqualTo(toUpsert.strField);
        assertThat(dto.boolField).isEqualTo(toUpsert.boolField);
        assertThat(dto.intField).isEqualTo(toUpsert.intField);

    }

    @Test
    public void create__elasticConsumer() {
        TestModelAToUpsert toUpsert = rndAToUpsert();

        //
        //
        String id = testARegister.create(toUpsert);
        //
        //

        // invoke exact consumer
        kafkaProducerSimulator.push(ModelAElasticConsumer.class);

        assertThat(id).isNotNull();

        TableRequest tableRequest = new TableRequest();
        tableRequest.strField = toUpsert.strField;

        List<TestModelAElastic> modelAElastics = testAElasticRegister.load(tableRequest, Paging.defaultPaging());

        // Тут в теории может быть записей больше, чем 1, поэтому проверяем не на 1, а на больше 0
        // Причина: strField у двоих записей может быть одинаковым
        assertThat(modelAElastics).hasSizeGreaterThan(0);

        boolean contains = modelAElastics.stream()
                .map(modelAElastic -> modelAElastic.id)
                .anyMatch(elasticModelId -> Objects.equals(elasticModelId, id));

        assertThat(contains).isTrue();

    }

}
