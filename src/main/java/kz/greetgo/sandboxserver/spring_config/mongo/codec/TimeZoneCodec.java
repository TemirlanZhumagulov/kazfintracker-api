package kz.greetgo.sandboxserver.spring_config.mongo.codec;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.TimeZone;

public class TimeZoneCodec implements Codec<TimeZone> {

  public static final CodecRegistry REGISTRY = new CodecRegistry() {

    final TimeZoneCodec codec = new TimeZoneCodec();

    @SuppressWarnings("unchecked")
    @Override
    public <T> Codec<T> get(Class<T> clazz) {
      return clazz == TimeZone.class ? (Codec<T>) codec : null;
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
      return get(clazz);
    }
  };

  private TimeZoneCodec() {}

  @Override
  public Class<TimeZone> getEncoderClass() {
    return TimeZone.class;
  }

  @Override
  public TimeZone decode(BsonReader reader, DecoderContext decoderContext) {
    var ID = reader.readString();
    return ID == null ? null : TimeZone.getTimeZone(ID);
  }

  @Override
  public void encode(BsonWriter writer, TimeZone value, EncoderContext encoderContext) {
    if (value == null) {
      return;
    }
    writer.writeString(value.getID());
  }

}
