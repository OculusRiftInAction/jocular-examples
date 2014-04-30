package org.saintandreas;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL11;
import org.saintandreas.gl.IndexedGeometry;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.buffers.VertexArray;
import org.saintandreas.gl.shaders.Attribute;
import org.saintandreas.gl.shaders.Program;
import org.saintandreas.gl.textures.Texture;
import org.saintandreas.math.Vector2f;
import org.saintandreas.math.Vector4f;
import org.saintandreas.spritz.TimedWord;

public class Font {
  private static final float DTP_TO_METERS = 0.003528f;
  private static final float METERS_TO_DTP = 1.0f / DTP_TO_METERS;

  // stores the font metrics for a single character
  public class Metrics {
    public Vector2f ul;
    public Vector2f size;
    public Vector2f offset;
    public float d; // xadvance - adjusts character positioning
    public int indexOffset;

    Rectangle2D getBounds() {
      return new Rectangle2D.Double(offset.x, offset.y, size.x, size.y);
    }

    Rectangle2D getTexCoords(Vector2f textureSize) {
      Vector2f tul = ul.divide(textureSize);
      Vector2f tsize = size.divide(textureSize);
      return new Rectangle2D.Double(tul.x, tul.y, tsize.x, tsize.y);
    }

  };

  String getFamily() {
    return mFamily;
  }

  float getAscent(float fontSize) {
    return mAscent * (fontSize / mFontSize);
  }

  // !
  float getDescent(float fontSize) {
    return mDescent * (fontSize / mFontSize);
  }

  // !
  float getLeading(float fontSize) {
    return mLeading * (fontSize / mFontSize);
  }

  float getSpaceWidth(float fontSize) {
    return mSpaceWidth * (fontSize / mFontSize);
  }

  boolean contains(Character charcode) {
    return mMetrics.containsKey(charcode);
  }

  final String mFamily;

  // ! calculated by the 'measure' function
  final float mFontSize;
  final float mLeading;
  final float mAscent;
  final float mDescent;
  final float mSpaceWidth;

  public final Texture mTexture;
  final IndexedGeometry mGeometry;
  final Map<Character, Metrics> mMetrics;

  private Font(ByteBuffer buffer) {
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    int header = buffer.getInt();
    System.out.println();
    if (0x46464453 != header) {
      throw new IllegalStateException("Bad header");
    }
    short version = buffer.getShort();

    // read font name
    if (version > 0x0001) {
      byte c = buffer.get();
      StringBuilder sb = new StringBuilder();
      while (0 != c) {
        sb.append((char) c);
        c = buffer.get();
      }
      mFamily = sb.toString();
    } else {
      mFamily = "";
    }

    // read font data
    mLeading = buffer.getFloat();
    mAscent = buffer.getFloat();
    mDescent = buffer.getFloat();
    mSpaceWidth = buffer.getFloat();
    mFontSize = mAscent + mDescent;

    short count = buffer.getShort();
    mMetrics = new HashMap<>();
    for (int i = 0; i < count; ++i) {
      Character charcode = buffer.getChar();
      Metrics m = new Metrics();
      mMetrics.put(charcode, m);
      m.ul = new Vector2f(buffer.getFloat(), buffer.getFloat());
      m.size = new Vector2f(buffer.getFloat(), buffer.getFloat());
      m.offset = new Vector2f(buffer.getFloat(), buffer.getFloat());
      m.d = buffer.getFloat();
    }

    Vector2f textureSize;
    {
      byte[] data = buffer.array();
      int start = buffer.position() + buffer.arrayOffset();
      ByteArrayInputStream is = new ByteArrayInputStream(data, start,
          data.length - start);
      BufferedImage image;
      try {
        image = ImageIO.read(is);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
      textureSize = new Vector2f(image.getWidth(), image.getHeight());
      mTexture = new Texture(GL11.GL_TEXTURE_2D);
      mTexture.bind();
      mTexture.parameter(GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
      mTexture.parameter(GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
      GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(),
          image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE,
          Texture.convertImageData(image));
      mTexture.unbind();
    }

    List<Vector4f> vertexData = new ArrayList<>();
    List<Short> indexData = new ArrayList<>();
    for (Character c : new TreeSet<>(mMetrics.keySet())) {
      Metrics m = mMetrics.get(c);
      Rectangle2D r = m.getBounds();
      Rectangle2D tr = m.getTexCoords(textureSize);
      int index = vertexData.size() / 2;
      vertexData.add(lowerLeft(r));
      vertexData.add(upperLeft(tr));
      vertexData.add(lowerRight(r));
      vertexData.add(upperRight(tr));
      vertexData.add(upperRight(r));
      vertexData.add(lowerRight(tr));
      vertexData.add(upperLeft(r));
      vertexData.add(lowerLeft(tr));

      m.indexOffset = indexData.size();
      indexData.add((short) (index + 0));
      indexData.add((short) (index + 1));
      indexData.add((short) (index + 2));
      indexData.add((short) (index + 0));
      indexData.add((short) (index + 2));
      indexData.add((short) (index + 3));
    }

    IndexedGeometry.Builder builder = new IndexedGeometry.Builder(indexData,
        vertexData);
    builder.withAttribute(Attribute.POSITION);
    builder.withAttribute(Attribute.TEX);
    mGeometry = builder.build();
  }

  static void read(byte[] data) throws IOException {
    read(ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN));
  }

  private static Vector4f lowerLeft(Rectangle2D rect) {
    return new Vector4f((float) rect.getMinX(), (float) rect.getMaxY(), 0, 0);
  }

  private static Vector4f lowerRight(Rectangle2D rect) {
    return new Vector4f((float) rect.getMaxX(), (float) rect.getMaxY(), 0, 0);
  }

  private static Vector4f upperLeft(Rectangle2D rect) {
    return new Vector4f((float) rect.getMinX(), (float) rect.getMinY(), 0, 0);
  }

  private static Vector4f upperRight(Rectangle2D rect) {
    return new Vector4f((float) rect.getMaxX(), (float) rect.getMinY(), 0, 0);
  }

  public static Font read(ByteBuffer buffer) {
    return new Font(buffer);
  }

  private static Program textProgram = null;

  private float renderCharacter(Character c, float offset, boolean rightAlign) {
    if (!contains(c)) {
      c = '?';
    }
    // get metrics for this character to speed up measurements
    Metrics m = mMetrics.get(c);
    // We create an offset vec2 to hold the local offset of this character
    // This includes compensating for the inverted Y axis of the font
    // coordinates

    // Bind the new position
    MatrixStack mv = MatrixStack.MODELVIEW;
    float d = rightAlign ? m.d : 0;
    mv.push();
    {
      mv.translate(new Vector2f(offset - d, -m.size.y));
      mv.bind(textProgram);
      // Render the item
      GL11.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_SHORT,
          m.indexOffset * 2);
    }
    mv.pop();
    return m.d;
  }

  private float renderCharacter(Character c, float offset) {
    return renderCharacter(c, offset, false);
  }

  public void renderSpritz(TimedWord currentWord, float fontSize) {
    if (currentWord.word.isEmpty()) {
      return;
    }
    float scale = DTP_TO_METERS * fontSize / mFontSize;
    if (textProgram == null) {
      textProgram = new Program(ExampleResource.SHADERS_TEXT_VS,
          ExampleResource.SHADERS_TEXT_FS);
      textProgram.link();
    }
    textProgram.use();
    textProgram.setUniform("Font", 0);
    MatrixStack.PROJECTION.bind(textProgram);

    mTexture.bind();
    mGeometry.bindVertexArray();

    MatrixStack mv = MatrixStack.MODELVIEW;
    mv.withPush(() -> {
      mv.translate(new Vector2f(-0.30f, scale * -(mAscent / 2.0f)));
      mv.scale(scale);
      textProgram.setUniform("Color", new Vector4f(0.8f, 0, 0.01f, 1));

      float advance = 0;
      advance += renderCharacter(currentWord.word.charAt(currentWord.orp),
          advance);
      textProgram.setUniform("Color", new Vector4f(1));
      for (int i = currentWord.orp + 1; i < currentWord.word.length(); ++i) {
        advance += renderCharacter(currentWord.word.charAt(i), advance);
      }
      advance = 0;
      for (int i = currentWord.orp - 1; i >= 0; --i) {
        advance -= renderCharacter(currentWord.word.charAt(i), advance, true);
      }
    });
    VertexArray.unbind();
    Texture.unbind(GL11.GL_TEXTURE_2D);
    Program.clear();
  }

}
