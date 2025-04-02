package adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        if (localDateTime != null) {
            jsonWriter.value(localDateTime.format(dtf));
        } else {
            jsonWriter.value((String) null);
        }
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        JsonToken token = jsonReader.peek();
        return switch (token) {
            case JsonToken.NULL -> {
                jsonReader.nextNull();
                yield null;
            }
            case JsonToken.STRING -> LocalDateTime.parse(jsonReader.nextString(), dtf);
            default -> throw new RuntimeException("Unexpected token type " + token.name());
        };
    }
}
