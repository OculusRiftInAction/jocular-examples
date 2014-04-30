package org.saintandreas.spritz;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.saintandreas.json.Json;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Text implements Iterable<TimedWord> {
  private static final String F = "V2";
  private static final String G = "AAAC";
  private static final float E = 1.21f;

  public final List<TimedWord> words;
  public final double duration;
  public final String locale;
  public final String version;

  @JsonIgnore
  private int currentIndex = 0;

  @JsonCreator
  private Text(@JsonProperty("words") List<TimedWord> words,
      @JsonProperty("duration") double duration,
      @JsonProperty("locale") String locale,
      @JsonProperty("version") String version) {
    this.words = ImmutableList.copyOf(words);
    this.duration = duration;
    this.locale = locale;
    this.version = version;
  }

  public static Text parseEncodedJson(String result) throws IOException {
    JsonNode node = Json.MAPPER.readTree(result);
    String a = node.path("sd0").asText();
    if (!F.equals(a)) {
      throw new RuntimeException("Unknown container format");
    }
    JsonNode b = node.path("sd1");
    JsonNode c = node.path("sd2");
    double d = node.path("duration").asDouble();
    if (null == b || !b.isArray() || null == c || !c.isArray()) {
      throw new RuntimeException("Invalid data format: wrong types");
    }
    if (0 == c.size()) {
      throw new RuntimeException("Invalid data format: data2");
    }
    String o = c.path(0).asText();
    if (null == o) {
      throw new RuntimeException("Invalid preamble");
    }
    String[] p = Iterables.toArray(Splitter.on(",").split(o), String.class);
    if (p.length < 4) {
      throw new RuntimeException("Invalid preamble");
    }
    if (!G.equals(p[0])) {
      throw new RuntimeException("Unrecognized encoding");
    }
    int q = Integer.parseInt(p[1]);
    if (b.size() != q || c.size() - 1 != q) {
      throw new RuntimeException("Invalid data format: Wrong data length");
    }
    List<TimedWord> r = new ArrayList<>();
    for (int s = 0; q > s; ++s) {
      String t = b.path(s).asText();
      String u = c.path(s + 1).asText();
      r.add(TimedWord.parse(t, u));
    }
    return new Text(r, d, p[2], p[3]);
  }

  @Override
  public Iterator<TimedWord> iterator() {
    return words.iterator();
  }

  public List<TimedWord> getWords() {
    return words;
  }

  public String getLocale() {
    return locale;
  }

  public String getVersion() {
    return version;
  }

  public TimedWord getWord(int a) {
    return words.get(a);
  }

  public TimedWord getCurrentWord() {
    return getWord(currentIndex);
  }

  public TimedWord getNextWord() {
    return null;
  }

  public int getCurrentIndex() {
    return currentIndex;
  }

  public void setCurrentIndex(int j) {
    this.currentIndex = j;
  }

  public void reset() {
    this.currentIndex = 0;
  }

  public int getPreviousSentenceStart() {
    // var c = SPRITZ.model;
    // if (0 > a || a >= f.length) throw new
    // c.ArrayIndexOutOfBoundsException(a);
    // var d = a;
    // if (b > 0) for (; d > 0 && b > 0 && (!f[d].isSentenceStart() || 0 !=
    // --b); d--);
    // return d
    return 0;
  }

  public int getNextSentenceStart() {
    // var c = SPRITZ.model;
    // if (0 > a || a >= f.length) throw new
    // c.ArrayIndexOutOfBoundsException(a);
    // var d = a;
    // if (b > 0) for (; d < f.length && (!f[d].isSentenceStart() || 0 != --b);
    // d++);
    // return d
    return 0;
  }

  public float calculateTime(int a, int b) {
    return Math.round(6e4 * duration * (words.size() - b)
        / (a * E * words.size()));
  }

  // }, this.getTotalTime = function (a) {
  // return this.calculateTime(a, 0)
  // }, this.getRemainingTime = function (a) {
  // return this.calculateTime(a, j)
  // }, this.getProgressTracker = function () {
  // return k
  // }, this.setProgressTracker = function (a) {
  // k = a
  // }

  private static final String SPRITZ_URL = "https://api.spritzinc.com/api-server/v1/misc/spritzify";
  private static final String BEARER_TOKEN = "Secret";
  
  public static String getSprizifyResponse(String text) throws IOException {
    String result;
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpPost poster = new HttpPost(SPRITZ_URL);
      HttpEntity entity = EntityBuilder
          .create()
          .setContentType(ContentType.APPLICATION_FORM_URLENCODED)
          .setParameters(new BasicNameValuePair("plainText", text),
              new BasicNameValuePair("locale", "en_us;")).build();
      poster.setEntity(entity);
      poster.setHeader("Authorization", "Bearer " + BEARER_TOKEN);
      System.out.println("Executing request: " + poster.getRequestLine());
      try (CloseableHttpResponse response = httpclient.execute(poster)) {
        if (200 != response.getStatusLine().getStatusCode()) {
          throw new IllegalStateException("Got invalid response code "
              + response.getStatusLine().toString());
        }
        result = new String(ByteStreams.toByteArray(response.getEntity()
            .getContent()));
      }
    }
    return result;
  }

  public static Text spritzify(String text) throws IOException {
    return Text.parseEncodedJson(getSprizifyResponse(text));
  }

  public static Text parseJson(String text) {
    return null;
  }

  public static Text spritzifyDebug(File f) throws IOException {
    String baseName = f.getName();
    File fp = f.getParentFile();
    String string = Files.toString(f, Charsets.UTF_8);
    String encoded = getSprizifyResponse(string);
    {
      File fe = new File(fp, baseName + ".encoded.json");
      Files.write(encoded, fe, Charsets.UTF_8);
    }
    Text t = parseEncodedJson(encoded);
    String raw = Json.prettyPrint(t);
    {
      File fr = new File(fp, baseName + ".raw.json");
      Files.write(raw, fr, Charsets.UTF_8);
    }
    Text t2 = Json.MAPPER.readValue(raw, Text.class);
    return t2;
  }

}
