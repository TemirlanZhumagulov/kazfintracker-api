package kz.greetgo.sandboxserver.spring_config.mongo.codec;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("rawtypes")
public class EnumCodec implements Codec {

  private final Class enumClass;
  private final Method nameMethod;

  public EnumCodec(Class enumClass) {
    this.enumClass = enumClass;

    try {

      //noinspection unchecked
      nameMethod = enumClass.getMethod("name");

    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public Object decode(BsonReader reader, DecoderContext decoderContext) {

    try {
      //noinspection unchecked
      return Enum.valueOf(enumClass, reader.readString());
    } catch (IllegalArgumentException ignore) {
      return null;
    }

  }

  @Override
  public void encode(BsonWriter writer, Object value, EncoderContext encoderContext) {

    try {

      writer.writeString((String) nameMethod.invoke(value));

    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException) cause;
      }
      throw new RuntimeException(cause);
    }

  }

  @Override
  public Class getEncoderClass() {
    return enumClass;
  }

}
