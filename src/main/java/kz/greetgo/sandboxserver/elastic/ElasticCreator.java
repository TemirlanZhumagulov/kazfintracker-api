package kz.greetgo.sandboxserver.elastic;

import kz.greetgo.sandboxserver.model.elastic.ClientElastic;
import kz.greetgo.sandboxserver.model.elastic.ElasticModel;
import kz.greetgo.sandboxserver.model.elastic.TestModelAElastic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class ElasticCreator {

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private ElasticWorker elasticWorker;

  private static final List<ElasticModel> ELASTIC_MODELS = List.of(new ElasticModel() {
    @Override
    public String indexName() {
      return ClientElastic.indexName();
    }

    @Override
    public String mapping() {
      return ClientElastic.mapping();
    }
  });

  public void createNeededIndexes() {
    createIndexes();
  }

  private void createIndexes() {
    ElasticCreator.ELASTIC_MODELS.stream()
      .filter(Predicate.not(elasticModel -> elasticWorker.doesIndexExists(elasticModel.indexName())))
      .forEach(elasticModel -> elasticWorker.createIndex(elasticModel.indexName(), elasticModel.mapping()));
  }

}
