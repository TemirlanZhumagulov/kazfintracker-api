package kz.kazfintracker.sandboxserver.elastic.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsHit {

  public String _index;

  public String _id;

  public double _score;

  public Map<String, String> _source;

}
