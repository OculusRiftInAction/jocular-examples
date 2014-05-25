package org.saintandreas.vr;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31.*;
import static org.saintandreas.ExampleResource.*;
import static org.saintandreas.vr.FooResource.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import javax.imageio.ImageIO;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.saintandreas.Font;
import org.saintandreas.gl.IndexedGeometry;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.OpenGL;
import org.saintandreas.gl.buffers.VertexArray;
import org.saintandreas.gl.shaders.Program;
import org.saintandreas.gl.textures.Texture;
import org.saintandreas.json.Json;
import org.saintandreas.math.Matrix4f;
import org.saintandreas.math.Quaternion;
import org.saintandreas.math.Vector3f;
import org.saintandreas.math.Vector4f;
import org.saintandreas.resources.Resource;
import org.saintandreas.resources.ResourceManager;
import org.saintandreas.scene.RootNode;
import org.saintandreas.scene.SceneNode;
import org.saintandreas.scene.ShaderNode;
import org.saintandreas.scene.TransformNode;
import org.saintandreas.spritz.Text;
import org.saintandreas.spritz.TimedWord;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.oculusvr.capi.OvrLibrary;
import com.oculusvr.capi.SensorState;

public class OliverRift extends RiftApp {
  // The farthest the user can turn their head away from
  // the control before it pauses
  private static final float MAX_ANG = 0.07f;
  // The farthest rate at which the user can turn their
  // head before the control pauses
  private static final float MAX_ANGV = 0.04f;
  // The json representation of a pre-spritzed piece of text
  private static final File SPRITZ_JSON = new File("spritzDemo.txt.raw.json");
  // private static final File SPRITZ_TEXT = new File("spritzDemo.txt");

  RootNode root = new RootNode();
  Font font;
  Text spritzedText;
  TimedWord currentWord = null;
  Iterator<TimedWord> itr = null;
  long nextWordTime = 0;
  boolean paused = false;
  boolean track = false;
  float uiAlpha = 0;
  float angV = 0;
  float ang = 0;
  long lastHighAngV = 0;
  Quaternion forward = new Quaternion();
  Quaternion current = new Quaternion();
  long startTimeMillis = System.currentTimeMillis();

  public static String loadResource(String source) throws IOException {
    return Resources.toString(Resources.getResource(source), Charsets.UTF_8);
  }

  public OliverRift() throws IOException {
    spritzedText = Json.MAPPER.readValue(Files.toString(SPRITZ_JSON, Charsets.UTF_8), Text.class);
    // spritzedText = Text.spritzifyDebug(SPRITZ_TEXT);
  }

  public int getWordsPerMinute() {
    return 400;
  }

  private SceneNode buildScene() {
    return new SceneNode(() -> {
      glDisable(GL_DEPTH_TEST);
    }, () -> {
      glEnable(GL_DEPTH_TEST);
    }).addChild(buildSkybox()).addChild(buildSpritzNode());
  }

  private static final Resource SKYBOX[] = {
      //
      IMAGES_SKY_CITY_XNEG_PNG, //
      IMAGES_SKY_CITY_XPOS_PNG, //
      IMAGES_SKY_CITY_YNEG_PNG, //
      IMAGES_SKY_CITY_YPOS_PNG, //
      IMAGES_SKY_CITY_ZNEG_PNG, //
      IMAGES_SKY_CITY_ZPOS_PNG //
  };

  private SceneNode buildSkybox() {
    Program program = new Program(SHADERS_CUBEMAP_VS, SHADERS_CUBEMAP_FS);
    Texture skybox = OpenGL.getCubemapTextures(SKYBOX);
    IndexedGeometry cube = OpenGL.makeColorCube();
    program.link();
    return new TransformNode(true).addChild( //
        new ShaderNode(program).addChild( //
            new SceneNode(() -> {
              glCullFace(GL_FRONT);
              MatrixStack.bindAll(program);
              cube.bindVertexArray();
              skybox.bind();
              cube.draw();
              skybox.unbind();
              VertexArray.unbind();
              glCullFace(GL_BACK);
            })));
  }

  private SceneNode buildSpritzNode() {
    SceneNode fixedUiNode = new TransformNode(() -> {
      MatrixStack.MODELVIEW.preRotate(forward);
    });

    IndexedGeometry quad = OpenGL.makeTexturedQuad();
    Program program = new Program(SHADERS_SIMPLE_VS, SHADERS_COLORED_FS);
    program.link();
    fixedUiNode.addChild(new TransformNode().addChild(new ShaderNode(program).addChild(new SceneNode(() -> {
      MatrixStack.PROJECTION.bind(program);
      quad.bindVertexArray();
      MatrixStack mv = MatrixStack.MODELVIEW;
      Vector3f background = new Vector3f(1.5f, 0.40f, 1.0f);
      mv.scale(background);
      mv.bind(program);
      program.setUniform("Color", new Vector4f(1, 1, 1, 1));
      quad.draw();
      mv.withPush(() -> {
        mv.scale(new Vector3f(0.96f, 0.65f, 1.0f));
        mv.bind(program);
        program.setUniform("Color", new Vector4f(0, 0, 0, 1));
        quad.draw();
      });
      mv.withPush(() -> {
        mv.scale(new Vector3f(0.4f, 0.85f, 1));
        mv.translate(-1.45f);
        mv.bind(program);
        program.setUniform("Color", new Vector4f(0, 0, 0, 1));
        quad.draw();
      });
      mv.withPush(() -> {
        mv.scale(new Vector3f(0.55f, 0.85f, 1));
        mv.translate(0.76f);
        mv.bind(program);
        program.setUniform("Color", new Vector4f(0, 0, 0, 1));
        quad.draw();
      });
    }))));
    fixedUiNode.addChild(new SceneNode(() -> {
      font.renderSpritz(currentWord, 24 * 4);
    }));
    return fixedUiNode;
  }

  private SceneNode buildUiRing() {
    IndexedGeometry quad = OpenGL.makeTexturedQuad();
    // Do the icons
    Program program = new Program(SHADERS_TEXTURED_VS, SHADERS_TEXTURED_FS);
    Resource icons[] = new Resource[] { IMAGES_ICONS_HOME_PNG, //
        IMAGES_ICONS_CHAT_PNG, //
        IMAGES_ICONS_SETTINGS_PNG, //
        IMAGES_ICONS_FACEBOOK_PNG, //
        IMAGES_ICONS_CAMERA_PNG, //
        IMAGES_ICONS_PLAY_PNG, //
        IMAGES_ICONS_FAVORITE_PNG, //
        IMAGES_ICONS_BOOK_PNG, //
    };
    Texture iconsTex[] = new Texture[icons.length];

    for (int i = 0; i < icons.length; ++i) {
      Resource r = icons[i];
      BufferedImage image;
      try {
        {
          ByteBuffer buffer = ResourceManager.getProvider().getAsByteBuffer(r);
          InputStream is = new ByteArrayInputStream(buffer.array());
          image = ImageIO.read(is);
        }
        iconsTex[i] = Texture.loadImage(image);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
      Texture t = iconsTex[i];
      t.bind();
      t.parameter(GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
      t.parameter(GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
      t.parameter(GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
      t.parameter(GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
      t.unbind();
    }
    float perIconAngle = (float) Math.PI * 2.0f / icons.length;
    Vector3f clockwiseAxis = Vector3f.UNIT_Z.scale(-1);
    Vector3f minuteHand = Vector3f.UNIT_Y.scale(3.5f);
    return new TransformNode().addChild(new ShaderNode(program).addChild(new SceneNode(() -> {
      if (uiAlpha == 0) {
        return;
      }
      MatrixStack.PROJECTION.bind(program);
      program.setUniform("Alpha", uiAlpha);
      quad.bindVertexArray();
      for (int i = 0; i < iconsTex.length; ++i) {
        MatrixStack mv = MatrixStack.MODELVIEW;
        Matrix4f m = new Matrix4f().rotate(i * perIconAngle, clockwiseAxis);
        mv.withPush(() -> {
          mv.translate(m.mult(minuteHand.scale(1 + (1 - uiAlpha) * 0.4f)));
          mv.scale(0.3f);
          mv.bind(program);
        });
        iconsTex[i].bind();
        quad.draw();
      }
      VertexArray.unbind();
      Texture.unbind(GL_TEXTURE_2D);
    })));
  }

  @Override
  protected void initGl() {
    super.initGl();
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_PRIMITIVE_RESTART);
    glDisable(GL_CULL_FACE);
    glPrimitiveRestartIndex(Short.MAX_VALUE);
    font = Font.read(ResourceManager.getProvider().getAsByteBuffer(FONTS_INCONSOLATA_MEDIUM_SDFF));
    root.addChild(buildScene());
  }

  @Override
  protected void update() {
    while (Keyboard.next()) {
      if (Keyboard.getEventKeyState()) {
        if (Keyboard.getEventKey() == Keyboard.KEY_R) {
          hmd.resetSensor();
        }
        if (Keyboard.getEventKey() == Keyboard.KEY_T) {
          track = !track;
        }
      }
    }

    // sensorFusion.Reset();
    SensorState sensor = hmd.getSensorState(OvrLibrary.INSTANCE.ovr_GetTimeInSeconds());
    current = RiftUtils.toQuaternion(sensor.Predicted.Pose.Orientation);
    angV = RiftUtils.toVector3f(sensor.Predicted.AngularVelocity).lengthSquared();
    ang = Math.abs(current.angleBetween(forward));
    if (angV > MAX_ANGV) {
      paused = true;
      lastHighAngV = System.currentTimeMillis();
    } else if (ang > MAX_ANG) {
      paused = true;
    } else {
      paused = false;
    }
    if (null == itr) {
      itr = this.spritzedText.iterator();
    }

    MatrixStack.MODELVIEW.lookat( //
        Vector3f.UNIT_Z.scale(8f), //
        Vector3f.ZERO, //
        Vector3f.UNIT_Y//
        );
    // MatrixStack.MODELVIEW.preRotate(current.inverse());
    if (paused) {
      nextWordTime = System.currentTimeMillis() + 600;
    }

    if (ang > MAX_ANG) {
      if (uiAlpha < 1.0f) {
        uiAlpha = Math.min(uiAlpha + 0.05f, 1);
      }
    } else {
      if (uiAlpha > 0.0f) {
        uiAlpha = Math.max(uiAlpha - 0.05f, 0);
      }
    }

    if (track && System.currentTimeMillis() - lastHighAngV > 750) {
      Quaternion delta = forward.mult(current.inverse());
      forward = forward.mult(new Quaternion().interpolate(delta, 0.02f).inverse());
      float euler[] = forward.toAngles(null);
      forward = Quaternion.fromAngles(euler[0], euler[1], euler[2]);
    }
    if (itr.hasNext()) {
      if (null == currentWord || (System.currentTimeMillis() > nextWordTime)) {
        currentWord = itr.next();
        nextWordTime = currentWord.getNextWordTimeMillis(getWordsPerMinute());
      }
    }
  }

  @Override
  protected void renderScene() {
    glEnable(GL_BLEND);
    glDisable(GL_DEPTH_TEST);
    glClearColor(0.1f, 0.1f, 0.1f, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    root.render();
  }

  public static void main(String[] args) throws IOException {
    new OliverRift().run();
  }

}
