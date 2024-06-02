package kz.kazfintracker.sandboxserver.elastic.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EsHitsWrapper {

  public EsInfo total;

  public double max_score;

  public List<EsHit> hits;

  public List<EsHit> hits() {
    return hits != null
      ? hits
      : (hits = new ArrayList<>());
  }

}
