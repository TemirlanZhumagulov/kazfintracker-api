package kz.greetgo.sandboxserver.model.mongo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.bson.types.ObjectId;

@FieldNameConstants
@Getter
@Setter
public class ImageDto {
  private ObjectId id;
  private String name;
  private byte[] data;

}
