package kz.greetgo.sandboxserver.spring_config.mongo.codec;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("rawtypes")
public class EnumCodecRegistry implements CodecRegistry {

  private final ConcurrentHashMap<Class, Codec> codecMap = new ConcurrentHashMap<>();

  @Override
  @SuppressWarnings("unchecked")
  public Codec get(Class clazz) {

    if (!isAnEnum(clazz)) {
      return null;
    }

    return codecMap.computeIfAbsent(clazz, EnumCodec::new);

  }

  private boolean isAnEnum(Class clazz) {
    if (clazz.isEnum()) {
      return true;
    }

    var superclass = clazz.getSuperclass();
    if (superclass == null) {
      return false;
    }

    return superclass.isEnum();
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
    return get(clazz);
  }
}
