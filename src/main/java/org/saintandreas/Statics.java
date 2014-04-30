package org.saintandreas;

import java.util.function.BiConsumer;

public class Statics {
  public static void forEach(int sizeX, int sizeY, BiConsumer<Integer, Integer> b) {
    for (int y = 0; y < sizeX; ++y) {
      for (int x = 0; x < sizeY; ++x) {
        b.accept(x, y);
      }
    }
  }

  public static void forEach(int size, BiConsumer<Integer, Integer> b) {
    forEach(size, size, b);
  }

}
