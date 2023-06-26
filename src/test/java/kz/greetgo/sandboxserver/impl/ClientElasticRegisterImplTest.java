package kz.greetgo.sandboxserver.impl;

import com.mongodb.client.MongoCollection;
import kz.greetgo.sandboxserver.ParentTestNG;
import kz.greetgo.sandboxserver.model.mongo.ClientDto;
import kz.greetgo.sandboxserver.model.web.upsert.ClientToUpsert;
import kz.greetgo.sandboxserver.mongo.MongoAccess;
import kz.greetgo.sandboxserver.register.ClientElasticRegister;
import kz.greetgo.sandboxserver.register.ClientRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClientElasticRegisterImplTest extends ParentTestNG {
    @Autowired
    ClientElasticRegister elasticRegister;
    @Autowired
    MongoAccess mongoAccess;
    @Autowired
    private ClientRegister clientRegister;
    private static final Logger logger = LoggerFactory.getLogger(ClientElasticRegisterImplTest.class);

    @Test
    public void getClientListCount(){
        int count = elasticRegister.getClientListCount();
        assertThat(count).isEqualTo(4);
    }
    @Test
    public void testLoadAllPaging(){
        MongoCollection<ClientDto> collection = mongoAccess.client();
        long initialCount = collection.countDocuments();
        System.out.println(" ------------------ Initial document count: " + initialCount);
        ClientToUpsert toUpsert = clientToUpsert();
        //
        //
        String[] ids = new String[]{clientRegister.create(toUpsert),
                clientRegister.create(toUpsert),
                clientRegister.create(toUpsert),
                clientRegister.create(toUpsert),
                clientRegister.create(toUpsert),
                clientRegister.create(toUpsert),
                clientRegister.create(toUpsert),
                clientRegister.create(toUpsert),
                clientRegister.create(toUpsert),
                clientRegister.create(toUpsert)
        };
        //
        //
        long afterInsertingCount = collection.countDocuments();
        System.out.println(" ------------------ After inserting all documents count: " + afterInsertingCount);
        assertThat(afterInsertingCount).isEqualTo(initialCount + 10);

//        List<ClientElastic> list = elasticRegister.loadAll(Paging.of(0, 7));
//        assertThat(list).isNotNull();
//        logger.info("The First page limit 10 {}", list);
//        assertThat(list.size()).isEqualTo(7);
//
//        List<ClientElastic> list2 = elasticRegister.loadAll(Paging.of(1, 3));
//        logger.info("The Second page limit 10 {}", list2);
//        assertThat(list2.size()).isEqualTo(3);
//
//        // Clear data inserted
//        IntStream.range(0, ids.length).forEach(i -> clientRegister.delete(ids[i]));
//        long afterDeletingCount = collection.countDocuments();
//        System.out.println(" ------------------ After deleting all documents count: " + afterDeletingCount);
//        assertThat(afterDeletingCount).isEqualTo(initialCount);
    }


    @Test
    public void testFiltered() {
        //...
    }
}
