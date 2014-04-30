package org.saintandreas.spritz;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class TimedWord {
  public final String word;
  public final int orp;
  public final int multiplier;
  public final int position;
  public int flags;

  private static final int POSITION_MASK = 0x3FFFFFFF;
  private static final int ORP_MASK = 0xF;
  private static final int MULTIPLIER_MASK = 0x3FF;
  private static final int FLAG_MASK = 0x7;

  public TimedWord(
      @JsonProperty("word")
      String word, 
      @JsonProperty("orp")
      int orp, 
      @JsonProperty("multiplier")
      int multiplier, 
      @JsonProperty("position")
      int position, 
      @JsonProperty("flags")
      int flags) {
    this.word = word;
    this.orp = orp;
    this.multiplier = multiplier;
    this.position = position;
    this.flags = flags;
  }

  public static TimedWord parse(String word, String u) {
    long decoded = Long.parseLong(u, 16);
    int flags = (int) (decoded & FLAG_MASK);
    int multiplier = (int) ((decoded >> 3) & MULTIPLIER_MASK);
    int orp = (int) ((decoded >> 17) & ORP_MASK);
    int position = (int) ((decoded >> 21) & POSITION_MASK);
    return new TimedWord(word, orp, multiplier, position, flags);
  }

  @JsonIgnore
  public boolean isBold() {
    return 0 != (flags & Flag.FLAG_BOLD.value);
  }

  @JsonIgnore
  public boolean isSentenceStart() {
    return 0 != (flags & Flag.FLAG_SENTENCE_START.value);
  }

  @JsonIgnore
  public boolean isParagraphStart() {
    return 0 != (flags & Flag.FLAG_PARAGRAPH_START.value);
  }

  public static double getStandardWordDuration(int wordsPerMinute) {
    return Math.floor(6e4 / (wordsPerMinute * 1.21));
  }

  public long getDurationMillis(int wordsPerMinute) {
    double vbr = getStandardWordDuration(wordsPerMinute);
    return (long) ((1.0 + multiplier / 100.0) * vbr);
  }

  public long getNextWordTimeMillis(int wordsPerMinute) {
    return System.currentTimeMillis() + getDurationMillis(wordsPerMinute);
  }

}
