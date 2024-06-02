package kz.kazfintracker.sandboxserver.impl.testing;

import kz.kazfintracker.sandboxserver.elastic.ElasticIndexes;
import kz.kazfintracker.sandboxserver.elastic.ElasticWorker;
import kz.kazfintracker.sandboxserver.elastic.model.EsBodyWrapper;
import kz.kazfintracker.sandboxserver.model.web.Paging;
import kz.kazfintracker.sandboxserver.model.elastic.TestModelAElastic;
import kz.kazfintracker.sandboxserver.model.web.TestTableRequest;
import kz.kazfintracker.sandboxserver.register.testing.TestAElasticRegister;
import kz.kazfintracker.sandboxserver.util.jackson.ObjectMapperHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
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
  public List<TestModelAElastic> load(TestTableRequest testTableRequest, Paging paging) {
    EsBodyWrapper bodyWrapper = elasticWorker.findModel(ElasticIndexes.INDEX_MODEL_A, testTableRequest.toMap(), paging);

    return bodyWrapper.hits.hits()
      .stream()
      .map(hit -> hit._source)
      .map(TestModelAElastic::fromMap)
      .collect(Collectors.toList());
  }

  @Override
  public void create(TestModelAElastic modelA) {
    log.info("elastic will create given data: " + modelA.strField + modelA.id);
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
