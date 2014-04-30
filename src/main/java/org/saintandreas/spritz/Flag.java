package org.saintandreas.spritz;

public enum Flag {
  FLAG_MASK(7),
  FLAG_SENTENCE_START(1),
  FLAG_BOLD(2),
  FLAG_PARAGRAPH_START(4);

  public final int value;

  Flag(int flag) {
    this.value = flag;
  }
}
