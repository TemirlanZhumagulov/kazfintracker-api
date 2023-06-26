package kz.greetgo.sandboxserver.impl;

import kz.greetgo.sandboxserver.elastic.ElasticIndexes;
import kz.greetgo.sandboxserver.elastic.ElasticWorker;
import kz.greetgo.sandboxserver.elastic.model.EsBodyWrapper;
import kz.greetgo.sandboxserver.model.Paging;
import kz.greetgo.sandboxserver.model.elastic.TestModelAElastic;
import kz.greetgo.sandboxserver.model.web.ClientsTableRequest;
import kz.greetgo.sandboxserver.model.web.TableRequest;
import kz.greetgo.sandboxserver.register.TestAElasticRegister;
import kz.greetgo.sandboxserver.util.jackson.ObjectMapperHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestAElasticRegisterImpl implements TestAElasticRegister {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private ElasticWorker elasticWorker;

  @Override
  public List<TestModelAElastic> loadAll(Paging paging) {
    EsBodyWrapper bodyWrapper = elasticWorker.findAll(ElasticIndexes.INDEX_MODEL_A, paging);

    return bodyWrapper.hits.hits()
      .stream()
      .map(hit -> hit._source)
      .map(TestModelAElastic::fromMap)
      .collect(Collectors.toList());
  }

  @Override
  public List<TestModelAElastic> load(TableRequest tableRequest, Paging paging) {
    EsBodyWrapper bodyWrapper = elasticWorker.find(ElasticIndexes.INDEX_MODEL_A, new ClientsTableRequest(), paging);

    return bodyWrapper.hits.hits()
      .stream()
      .map(hit -> hit._source)
      .map(TestModelAElastic::fromMap)
      .collect(Collectors.toList());
  }

  @Override
  public void create(TestModelAElastic modelA) {
    elasticWorker.insertDocument(ElasticIndexes.INDEX_MODEL_A, modelA.id, ObjectMapperHolder.writeJson(modelA));
  }

  @Override
  public void update(TestModelAElastic modelA) {
    elasticWorker.updateDocument(ElasticIndexes.INDEX_MODEL_A, modelA.id, ObjectMapperHolder.writeJson(modelA));
  }

  @Override
  public void delete(String id) {
    elasticWorker.deleteDocument(ElasticIndexes.INDEX_MODEL_A, id);
  }

}
