package kz.kazfintracker.sandboxserver.impl.table;


import kz.kazfintracker.sandboxserver.elastic.model.ClientResponse;
import kz.kazfintracker.sandboxserver.elastic.ElasticIndexes;
import kz.kazfintracker.sandboxserver.elastic.ElasticWorker;
import kz.kazfintracker.sandboxserver.elastic.model.EsBodyWrapper;
import kz.kazfintracker.sandboxserver.model.web.Paging;
import kz.kazfintracker.sandboxserver.model.elastic.ClientElastic;
import kz.kazfintracker.sandboxserver.model.web.ClientsTableRequest;
import kz.kazfintracker.sandboxserver.register.ClientElasticRegister;
import kz.kazfintracker.sandboxserver.util.jackson.ObjectMapperHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@Slf4j
public class ClientElasticRegisterImpl implements ClientElasticRegister {
    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired
    private ElasticWorker elasticWorker;

    @Override
    public ClientResponse loadAll(Paging paging) {
        log.info("Starting to load all");
        EsBodyWrapper bodyWrapper = elasticWorker.findAll(ElasticIndexes.INDEX_CLIENT, paging);
        log.info("EsBodyWrapper is received in Load ALL method");
        return new ClientResponse(bodyWrapper.hits.hits()
                .stream()
                .map(hit -> hit._source)
                .map(ClientElastic::fromMap)
                .collect(Collectors.toList()), bodyWrapper.hits.total.value);
    }
    @Override
    public ClientResponse load(ClientsTableRequest tableRequest, Paging paging) {
        log.info("ClientTableRequest's sorting is received: " + tableRequest.sorting);
        log.info("ClientTableRequest's rndTestingId is received: " + tableRequest.rndTestingId);
        EsBodyWrapper bodyWrapper = elasticWorker.find(ElasticIndexes.INDEX_CLIENT, tableRequest, paging);
        log.info("EsBodyWrapper is made: " + bodyWrapper.toString());
        return new ClientResponse(bodyWrapper.hits.hits()
                .stream()
                .map(hit -> hit._source)
                .map(ClientElastic::fromMap)
                .collect(Collectors.toList()),  bodyWrapper.hits.total.value);
    }

    @Override
    public void create(ClientElastic client) {
        elasticWorker.insertDocument(ElasticIndexes.INDEX_CLIENT, client.id, ObjectMapperHolder.writeJson(client));
    }

    @Override
    public void update(ClientElastic client) {
        elasticWorker.updateDocument(ElasticIndexes.INDEX_CLIENT, client.id, ObjectMapperHolder.writeJson(client));
    }

    @Override
    public void delete(String id) {
        elasticWorker.deleteDocument(ElasticIndexes.INDEX_CLIENT, id);
    }

    @Override
    public int getClientListCount() {
        return elasticWorker.countDocuments(ElasticIndexes.INDEX_CLIENT);
    }

}
