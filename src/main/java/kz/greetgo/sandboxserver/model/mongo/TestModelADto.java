package kz.greetgo.sandboxserver.model.mongo;

import kz.greetgo.sandboxserver.model.web.read.TestModelAToRead;
import kz.greetgo.sandboxserver.model.web.upsert.TestModelAToUpsert;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;

import static java.util.Objects.requireNonNullElse;

@FieldNameConstants
public class TestModelADto {

  public ObjectId id;

  public String strField;

  public Boolean boolField;

  public Integer intField;

  public boolean boolField() {
    return requireNonNullElse(boolField, false);
  }

  public int intField() {
    return requireNonNullElse(intField, 0);
  }

  public String strId() {
    return id.toString();
  }

  public static TestModelADto from(ObjectId id, TestModelAToUpsert testModelAWeb) {
    TestModelADto dto = new TestModelADto();

    dto.id = id;
    dto.strField = testModelAWeb.strField;
    dto.boolField = testModelAWeb.boolField;
    dto.intField = testModelAWeb.intField;

    return dto;
  }

  public TestModelAToRead toRead() {
    TestModelAToRead toRead = new TestModelAToRead();

    toRead.id = strId();
    toRead.strField = strField;
    toRead.boolField = boolField();
    toRead.intField = intField();

    return toRead;
  }

}
