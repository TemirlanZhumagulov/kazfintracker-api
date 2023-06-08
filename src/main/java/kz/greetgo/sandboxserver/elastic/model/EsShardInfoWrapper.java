package kz.greetgo.sandboxserver.elastic.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsShardInfoWrapper {

  public int total;

  public int successful;

  public int skipped;

  public int failed;

}
