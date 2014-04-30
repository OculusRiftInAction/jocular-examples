package org.saintandreas.json;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Json {
  public static final ObjectMapper MAPPER = new ObjectMapper();
  static {
    MAPPER.setVisibilityChecker(MAPPER.getSerializationConfig().getDefaultVisibilityChecker()
        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
  }
  public static JsonNode parseJson(String json) throws IOException {
    return MAPPER.readTree(json);
  }

  public static String format(Object o) throws IOException {
    return MAPPER.writeValueAsString(o);
  }

  public static String prettyPrint(Object o) throws IOException {
    return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(o);
  }
}
