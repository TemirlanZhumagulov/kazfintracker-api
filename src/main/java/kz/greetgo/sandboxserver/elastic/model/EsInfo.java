package kz.greetgo.sandboxserver.elastic.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsInfo {

  public int value;

  public String relation;

}
