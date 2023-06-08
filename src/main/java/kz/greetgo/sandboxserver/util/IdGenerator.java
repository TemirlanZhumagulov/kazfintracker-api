package kz.greetgo.sandboxserver.util;

import org.bson.types.ObjectId;

import java.security.SecureRandom;

public class IdGenerator {

  private final static ThreadLocal<SecureRandom> rnd = ThreadLocal.withInitial(SecureRandom::new);

  public static ObjectId generate() {
    byte[] bytes = new byte[12];
    rnd.get().nextBytes(bytes);
    return new ObjectId(bytes);
  }

}
