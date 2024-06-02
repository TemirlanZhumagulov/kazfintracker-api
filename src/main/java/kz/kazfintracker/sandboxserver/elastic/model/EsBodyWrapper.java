package kz.kazfintracker.sandboxserver.elastic.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsBodyWrapper {

  public int took;

  public boolean timed_out;

  public EsShardInfoWrapper _shards;

  public EsHitsWrapper hits;

}
