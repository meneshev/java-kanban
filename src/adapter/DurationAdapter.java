package adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        if (duration != null) {
            jsonWriter.value(duration.toMinutes());
        } else {
            jsonWriter.value((String) null);
        }
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.peek();
        return switch (token) {
            case JsonToken.NULL -> {
                jsonReader.nextNull();
                yield null;
            }
            case JsonToken.NUMBER -> Duration.ofMinutes(jsonReader.nextInt());
            default -> throw new RuntimeException("Unexpected token type " + token.name());
        };
    }
}