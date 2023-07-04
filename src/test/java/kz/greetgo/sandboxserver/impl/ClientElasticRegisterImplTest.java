package kz.greetgo.sandboxserver.impl;

import com.mongodb.client.MongoCollection;
import kz.greetgo.sandboxserver.ParentTestNG;
import kz.greetgo.sandboxserver.elastic.model.ClientResponse;
import kz.greetgo.sandboxserver.kafka.consumer.ClientConsumer;
import kz.greetgo.sandboxserver.model.Paging;
import kz.greetgo.sandboxserver.model.elastic.ClientElastic;
import kz.greetgo.sandboxserver.model.mongo.ClientDto;
import kz.greetgo.sandboxserver.model.web.ClientsTableRequest;
import kz.greetgo.sandboxserver.model.web.upsert.ClientToUpsert;
import kz.greetgo.sandboxserver.mongo.MongoAccess;
import kz.greetgo.sandboxserver.register.ClientElasticRegister;
import kz.greetgo.sandboxserver.register.ClientRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

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
    public void createElasticClient() {
        ClientToUpsert toUpsert = clientToUpsert();

        //
        //
        String id = clientRegister.create(toUpsert);
        //
        //

        // invoke exact consumer
        kafkaProducerSimulator.push(ClientConsumer.class);

        assertThat(id).isNotNull();

        ClientsTableRequest tableRequest = new ClientsTableRequest();
        tableRequest.full_name="Zh";

        ClientResponse response = elasticRegister.load(tableRequest, Paging.defaultPaging());

        assertThat(response.getClients()).hasSizeGreaterThan(0);

        boolean contains = response.getClients().stream()
                .map(clientElastic -> clientElastic.id)
                .anyMatch(clientElasticId -> Objects.equals(clientElasticId, id));

        assertThat(contains).isTrue();

    }

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
        ClientResponse response = elasticRegister.loadAll(Paging.of(10,0));
        assertThat(response.getCount()).isEqualTo(initialCount);

        ClientToUpsert toUpsert = clientToUpsert();
        //
        //
        String[] ids = new String[]{
                clientRegister.create(toUpsert),
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
        ClientResponse response2 = elasticRegister.loadAll(Paging.of(10,0));
        assertThat(response2.getCount()).isEqualTo(afterInsertingCount);

//        List<ClientElastic> list = elasticRegister.loadAll(Paging.of(0, 7));
//        assertThat(list).isNotNull();
//        logger.info("The First page limit 10 {}", list);
//        assertThat(list.size()).isEqualTo(7);
//
//        List<ClientElastic> list2 = elasticRegister.loadAll(Paging.of(1, 3));
//        logger.info("The Second page limit 10 {}", list2);
//        assertThat(list2.size()).isEqualTo(3);
//
        // Clear data inserted
        IntStream.range(0, ids.length).forEach(i -> clientRegister.delete(ids[i]));
        long afterDeletingCount = collection.countDocuments();
        System.out.println(" ------------------ After deleting all documents count: " + afterDeletingCount);
        assertThat(afterDeletingCount).isEqualTo(initialCount);
        ClientResponse response3 = elasticRegister.loadAll(Paging.of(10,0));
        assertThat(response3.getCount()).isEqualTo(afterDeletingCount);

    }


    @Test
    public void sortFullNameAsc() {
        // Data prep
        String uniqueTestingId = "sortFullNameAsc2";
        List<ClientToUpsert> clients = getTestClients(uniqueTestingId);
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            logger.info("Client Is Created " + clients.get(i));
            //
            //
        }
        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting = new HashMap<>();
        ctr.sorting.put("full_name", true);
        ctr.rndTestingId = uniqueTestingId;
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,10));
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        for (int i = 0; i < 10; i++) {
            assertThat(clientElastics.get(i).full_name).isEqualTo(clients.get(i).getSurname() + " " + clients.get(i).getName() + " " + clients.get(i).getPatronymic());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }
    @Test
    public void sortFullNameDesc(){
        // Data prep
        List<ClientToUpsert> clients = getTestClients("sortFullNameDesc1");
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            //
            //
        }

        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting.put("full_name", true);
        ctr.rndTestingId = "sortFullNameDesc";
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,10));
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        for (int i = 0; i < 10; i++) {
            assertThat(clientElastics.get(i).full_name).isEqualTo(clients.get(9 - i).getSurname() + " " + clients.get(9-i).getName() + " " + clients.get(9 - i).getPatronymic());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }

    @Test
    public void sortAgeAsc(){

    }
    @Test
    public void sortAgeDesc(){

    }
    @Test
    public void sort(){

    }

    @Test
    public void testFiltered() {
        //...
    }
}
