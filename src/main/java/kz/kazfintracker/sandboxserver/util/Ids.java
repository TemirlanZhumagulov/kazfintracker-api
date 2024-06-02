package kz.kazfintracker.sandboxserver.util;

import org.bson.types.ObjectId;
import org.mapstruct.Named;

import java.security.SecureRandom;

public class Ids {

  private final static ThreadLocal<SecureRandom> rnd = ThreadLocal.withInitial(SecureRandom::new);

  public static ObjectId generate() {
    byte[] bytes = new byte[12];
    rnd.get().nextBytes(bytes);
    return new ObjectId(bytes);
  }

  @Named("intToObjectId")
  public static ObjectId intToObjectId(int input) {
    byte[] objectIdBytes = new ObjectId().toByteArray();
    objectIdBytes[9] = (byte) (input >> 16);
    objectIdBytes[10] = (byte) (input >> 8);
    objectIdBytes[11] = (byte) input;
    return new ObjectId(objectIdBytes);
  }

  @Named("objectIdToInt")
  public static int objectIdToInt(ObjectId objectId) {
    byte[] objectIdBytes = objectId.toByteArray();
    return ((objectIdBytes[9] & 0xFF) << 16) | ((objectIdBytes[10] & 0xFF) << 8) | (objectIdBytes[11] & 0xFF);
  }

  public static void main(String[] args) {
    int myInt = 1;
    ObjectId objectId = intToObjectId(myInt);
    System.out.println("Generated ObjectId: " + objectId.toHexString());

    int extractedInt = objectIdToInt(objectId);
    System.out.println("Extracted int: " + extractedInt);
  }

}
