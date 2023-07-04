package kz.greetgo.sandboxserver.impl;

import kz.greetgo.sandboxserver.ParentTestNG;
import kz.greetgo.sandboxserver.elastic.model.ClientResponse;
import kz.greetgo.sandboxserver.kafka.consumer.ClientConsumer;
import kz.greetgo.sandboxserver.model.Paging;
import kz.greetgo.sandboxserver.model.elastic.ClientElastic;
import kz.greetgo.sandboxserver.model.web.ClientsTableRequest;
import kz.greetgo.sandboxserver.model.web.upsert.ClientToUpsert;
import kz.greetgo.sandboxserver.mongo.MongoAccess;
import kz.greetgo.sandboxserver.register.ClientElasticRegister;
import kz.greetgo.sandboxserver.register.ClientRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.Period;
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
    public void deleteElasticClient(){
        ClientToUpsert toUpsert = clientToUpsert();
        toUpsert.setRndTestingId("deleteElasticClient");
        //
        //
        String id = clientRegister.create(toUpsert);
        //
        //

        kafkaProducerSimulator.push(ClientConsumer.class);

        assertThat(id).isNotNull();

        ClientsTableRequest tableRequest = new ClientsTableRequest();
        tableRequest.rndTestingId="deleteElasticClient";

        ClientResponse response = elasticRegister.load(tableRequest, Paging.defaultPaging());

        assertThat(response.getClients()).hasSizeGreaterThan(0);

        elasticRegister.delete(id);

        ClientResponse response2 = elasticRegister.load(tableRequest, Paging.defaultPaging());

        assertThat(response2.getClients()).hasSizeLessThanOrEqualTo(0);
    }

    @Test
    public void testLoadAllTotalValue(){
        ClientResponse response = elasticRegister.loadAll(Paging.defaultPaging());
        int initialCount = elasticRegister.getClientListCount();
        assertThat(response.getTotal()).isEqualTo(initialCount);

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
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse response2 = elasticRegister.loadAll(Paging.defaultPaging());
        //
        //
        int afterInsertCount = elasticRegister.getClientListCount();
        assertThat(response2.getTotal()).isEqualTo(afterInsertCount);

        // Clear data inserted
        IntStream.range(0, ids.length).forEach(i -> clientRegister.delete(ids[i]));
        ClientResponse response3 = elasticRegister.loadAll(Paging.defaultPaging());
        int afterDeletingCount = elasticRegister.getClientListCount();
        assertThat(response3.getTotal()).isEqualTo(afterDeletingCount);
    }

    @Test
    public void testLoadAllPaging(){
        List<ClientToUpsert> toUpsert = getTestClients("testLoadAllPaging");
        String[] ids = new String[toUpsert.size()];

        for (int i = 0; i < toUpsert.size(); i++) {
            //
            //
            ids[i] = clientRegister.create(toUpsert.get(i));
            //
            //
        }
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse response = elasticRegister.loadAll(Paging.of(0,7));
        //
        //
        List<ClientElastic> clients = response.getClients();
        assertThat(clients).isNotNull();
        assertThat(clients.size()).isEqualTo(7);

        ClientResponse response2 = elasticRegister.loadAll(Paging.of(1,5));
        List<ClientElastic> clients2 = response2.getClients();
        assertThat(clients2).isNotNull();
        assertThat(clients2.size()).isEqualTo(5);

        // Clear data inserted
        IntStream.range(0, ids.length).forEach(i -> clientRegister.delete(ids[i]));
        ClientResponse response5 = elasticRegister.loadAll(Paging.of(0,10));
        int afterDeletingCount = elasticRegister.getClientListCount();
        assertThat(response5.getTotal()).isEqualTo(afterDeletingCount);
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
        kafkaProducerSimulator.push(ClientConsumer.class);
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
        String uniqueTestingId = "sortFullNameDesc2";
        List<ClientToUpsert> clients = getTestClients(uniqueTestingId);
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            //
            //
        }

        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting = new HashMap<>();
        ctr.sorting.put("full_name", false);
        ctr.rndTestingId = uniqueTestingId;
        kafkaProducerSimulator.push(ClientConsumer.class);

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
        // Data prep
        String uniqueTestingId = "sortAgeAsc2";
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
        ctr.sorting.put("age", true);
        ctr.rndTestingId = uniqueTestingId;
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,10));
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        for (int i = 0; i < 10; i++) {
            assertThat(Integer.parseInt(clientElastics.get(i).age)).isEqualTo(Period.between(clients.get(9 - i).getBirth_date(), LocalDate.now()).getYears());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }
    @Test
    public void sortAgeDesc(){
// Data prep
        String uniqueTestingId = "sortAgeDesc";
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
        ctr.sorting.put("age", false);
        ctr.rndTestingId = uniqueTestingId;
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,10));
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        for (int i = 0; i < 10; i++) {
            assertThat(Integer.parseInt(clientElastics.get(i).age)).isEqualTo(Period.between(clients.get(i).getBirth_date(), LocalDate.now()).getYears());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }
    @Test
    public void sortCharmAsc(){
        // Data prep
        String uniqueTestingId = "sortCharmAsc2";
        List<ClientToUpsert> clients = getTestClients(uniqueTestingId);
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            //
            //
        }
        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting = new HashMap<>();
        ctr.sorting.put("charm", true);
        ctr.rndTestingId = uniqueTestingId;
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,10));
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        for (int i = 0; i < 10; i++) {
            assertThat(clientElastics.get(i).charm).isEqualTo(clients.get(i).getCharm().getName());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }

    @Test
    public void sortCharmDesc(){
        // Data prep
        String uniqueTestingId = "sortCharmDesc";
        List<ClientToUpsert> clients = getTestClients(uniqueTestingId);
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            //
            //
        }
        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting = new HashMap<>();
        ctr.sorting.put("charm", false);
        ctr.rndTestingId = uniqueTestingId;
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,10));
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        for (int i = 0; i < 10; i++) {
            assertThat(clientElastics.get(i).charm).isEqualTo(clients.get(9 - i).getCharm().getName());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }

    @Test
    public void sortTotalBalanceAsc(){
        // Data prep
        String uniqueTestingId = "sortTotalBalanceAsc";
        List<ClientToUpsert> clients = getTestClients(uniqueTestingId);
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            //
            //
        }
        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting = new HashMap<>();
        ctr.sorting.put("total_balance", true);
        ctr.rndTestingId = uniqueTestingId;
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,10));
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        for (int i = 0; i < 10; i++) {
            assertThat(Float.parseFloat(clientElastics.get(i).total_balance)).isEqualTo(clients.get(i).getAccount().getTotal_balance());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }
    @Test
    public void sortTotalBalanceDesc(){
        // Data prep
        String uniqueTestingId = "sortTotalBalanceDesc";
        List<ClientToUpsert> clients = getTestClients(uniqueTestingId);
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            //
            //
        }
        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting = new HashMap<>();
        ctr.sorting.put("total_balance", false);
        ctr.rndTestingId = uniqueTestingId;
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,10));
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        for (int i = 0; i < 10; i++) {
            assertThat(Float.parseFloat(clientElastics.get(i).total_balance)).isEqualTo(clients.get(9 - i).getAccount().getTotal_balance());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }
    @Test
    public void sortMaxBalanceAsc(){
        // Data prep
        String uniqueTestingId = "sortMaxBalanceAsc";
        List<ClientToUpsert> clients = getTestClients(uniqueTestingId);
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            //
            //
        }
        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting = new HashMap<>();
        ctr.sorting.put("max_balance", true);
        ctr.rndTestingId = uniqueTestingId;
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,10));
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        for (int i = 0; i < 10; i++) {
            assertThat(Float.parseFloat(clientElastics.get(i).max_balance)).isEqualTo(clients.get(i).getAccount().getMax_balance());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }
    @Test
    public void sortMaxBalanceDesc(){
        // Data prep
        String uniqueTestingId = "sortMaxBalanceDesc";
        List<ClientToUpsert> clients = getTestClients(uniqueTestingId);
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            //
            //
        }
        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting = new HashMap<>();
        ctr.sorting.put("max_balance", false);
        ctr.rndTestingId = uniqueTestingId;
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,10));
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        for (int i = 0; i < 10; i++) {
            assertThat(Float.parseFloat(clientElastics.get(i).max_balance)).isEqualTo(clients.get(9 - i).getAccount().getMax_balance());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }
    @Test
    public void sortMinBalanceAsc(){
        // Data prep
        String uniqueTestingId = "sortMinBalanceAsc";
        List<ClientToUpsert> clients = getTestClients(uniqueTestingId);
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            //
            //
        }
        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting = new HashMap<>();
        ctr.sorting.put("min_balance", true);
        ctr.rndTestingId = uniqueTestingId;
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,10));
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        for (int i = 0; i < 10; i++) {
            assertThat(Float.parseFloat(clientElastics.get(i).min_balance)).isEqualTo(clients.get(i).getAccount().getMin_balance());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }
    @Test
    public void sortMinBalanceDesc(){
        // Data prep
        String uniqueTestingId = "sortMinBalanceDesc";
        List<ClientToUpsert> clients = getTestClients(uniqueTestingId);
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            //
            //
        }
        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting = new HashMap<>();
        ctr.sorting.put("min_balance", false);
        ctr.rndTestingId = uniqueTestingId;
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,10));
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        for (int i = 0; i < 10; i++) {
            assertThat(Float.parseFloat(clientElastics.get(i).min_balance)).isEqualTo(clients.get(9 - i).getAccount().getMin_balance());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }
    @Test
    public void testPaginationWithSorting() {
        // Data prep
        String uniqueTestingId = "testLoadPagination4";
        List<ClientToUpsert> clients = getTestClients(uniqueTestingId);
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            //
            //
        }
        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting = new HashMap<>();
        ctr.sorting.put("charm", true);
        ctr.rndTestingId = uniqueTestingId;
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,5));
        logger.info("t1VY0WZUXL :: " + clientResponse.getClients().toString());
        ClientResponse clientResponse2 = elasticRegister.load(ctr, Paging.of(5,5));
        logger.info("OJIs4s9PLX :: " + clientResponse2.getClients().toString());
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        List<ClientElastic> clientElastics2 = clientResponse2.getClients();
        for (int i = 0; i < 5; i++) {
            assertThat(clientElastics.get(i).charm).isEqualTo(clients.get(i).getCharm().getName());
        }
        for (int i = 0; i < 5; i++) {
            assertThat(clientElastics2.get(i).charm).isEqualTo(clients.get(i + 5).getCharm().getName());
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }

    @Test
    public void testPaginationWithFilter() {
        // Data prep
        String uniqueName = "UNIQUE NAME";
        List<ClientToUpsert> clients = getTestClientsForFilter(uniqueName);
        String[] idArray = new String[10];
        for (int i = 0; i < 10; i++) {
            //
            //
            idArray[i] = clientRegister.create(clients.get(i));
            //
            //
        }
        ClientsTableRequest ctr = new ClientsTableRequest();
        ctr.sorting = new HashMap<>();
        ctr.sorting.put("charm", true);
        ctr.full_name = uniqueName;
        kafkaProducerSimulator.push(ClientConsumer.class);
        //
        //
        ClientResponse clientResponse = elasticRegister.load(ctr, Paging.of(0,3));
        logger.info("t1VY0WZUXL :: " + clientResponse.getClients().toString());
        //
        //
        List<ClientElastic> clientElastics = clientResponse.getClients();
        assertThat(clientElastics.size()).isEqualTo(3);
        for (int i = 0; i < 3; i++) {
            assertThat(clientElastics.get(i).charm).isEqualTo(clients.get(i).getCharm().getName());
            assertThat(clientElastics.get(i).full_name).isEqualTo(uniqueName + " " + uniqueName + " " + uniqueName);
        }
        // Data clean
        for (int i = 0; i < 10; i++) {
            clientRegister.delete(idArray[i]);
        }
    }
}
