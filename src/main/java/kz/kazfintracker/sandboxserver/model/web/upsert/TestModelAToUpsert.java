package kz.kazfintracker.sandboxserver.model.web.upsert;

import org.bson.types.ObjectId;

public class TestModelAToUpsert {

  public String id;

  public String strField;

  public Boolean boolField;

  public Integer intField;

  public ObjectId objectId() {
    return new ObjectId(id);
  }

}
