package kz.greetgo.sandboxserver.impl;

import com.mongodb.client.model.Filters;
import kz.greetgo.sandboxserver.ParentTestNG;
import kz.greetgo.sandboxserver.model.mongo.TestModelADto;
import kz.greetgo.sandboxserver.model.web.upsert.TestModelAToUpsert;
import kz.greetgo.sandboxserver.mongo.MongoAccess;
import kz.greetgo.sandboxserver.register.TestARegister;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TestARegisterImplTest extends ParentTestNG {

    @Autowired
    private TestARegister testARegister;

    @Autowired
    private MongoAccess mongoAccess;

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



}
