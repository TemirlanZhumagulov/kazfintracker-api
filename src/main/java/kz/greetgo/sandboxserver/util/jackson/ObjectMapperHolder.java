package kz.greetgo.sandboxserver.util.jackson;

import ch.qos.logback.core.net.server.Client;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class is created with re-use purposes of the ObjectMapper instance.
 * ObjectMapper is immutable (thus thread-safe) class, but it is quite expensive to create.
 * Use static methods from class instead of creating ObjectMapper's instances all over the project
 */
public final class ObjectMapperHolder {

  private static final ObjectMapper mapper = new ObjectMapper();

  private ObjectMapperHolder() {
  }

  public static String writeJson(Object value) {
    try {
      mapper.findAndRegisterModules();
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public static <T> T readJson(String json, Class<T> valueType) {
    try {
      return mapper.readValue(json, valueType);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

}
