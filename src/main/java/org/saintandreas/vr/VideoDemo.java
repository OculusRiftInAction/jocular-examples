package org.saintandreas.vr;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.saintandreas.ExampleResource;
import org.saintandreas.gl.FrameBuffer;
import org.saintandreas.gl.IndexedGeometry;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.OpenGL;
import org.saintandreas.gl.buffers.IndexBuffer;
import org.saintandreas.gl.buffers.VertexArray;
import org.saintandreas.gl.shaders.Program;
import org.saintandreas.gl.textures.Texture;
import org.saintandreas.math.Vector3f;
import org.saintandreas.vr.oculus.RiftApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.binding.LibVlcFactory;
import uk.co.caprica.vlcj.binding.internal.libvlc_media_t;
import uk.co.caprica.vlcj.player.AudioDevice;
import uk.co.caprica.vlcj.player.AudioOutput;
import uk.co.caprica.vlcj.player.MediaPlayer;
import uk.co.caprica.vlcj.player.MediaPlayerEventListener;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class VideoDemo extends RiftApp implements BufferFormatCallback, RenderCallback, MediaPlayerEventListener {
  // private static final String MEDIA_URL = "http://192.168.0.4/sc2a.mp4";
  // private static final String MEDIA_URL =
  // "http://192.168.0.4/Videos/South.Park.S17E09.HDTV.x264-ASAP.%5bVTV%5d.mp4";
  // private static final String MEDIA_URL =
  // "http://192.168.0.4/Download/Gravity.2013.1080p%203D.HDTV.x264.DTS-RARBG/Gravity.2013.1080p%203D.HDTV.x264.DTS-RARBG.mkv";
  // private static final String MEDIA_URL =
  // "http://192.168.0.4/Videos/3D/TRON%20LEGACY%203D.mkv";
  private static final String MEDIA_URL = "http://192.168.0.4/Videos/3D/Man.Of.Steel.3D.2013.1080p.BluRay.Half-OU.DTS.x264-PublicHD.mkv";
  private static final Logger LOG = LoggerFactory.getLogger(VideoDemo.class);
  IndexedGeometry cubeGeometry;
  IndexedGeometry eyeMeshes[] = new IndexedGeometry[2];
  IndexedGeometry screenQuad;
  Program coloredProgram;
  Program textureProgram;
  FrameBuffer frameBuffer;
  Texture videoTexture;
  ByteBuffer videoData;
  MediaPlayer player;
  volatile boolean newFrame = false;
  int videoWidth, videoHeight;
  private float videoAspect;

  public VideoDemo() {
    // Rift applications should have no window decroation
    System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
    LibVlc libvlc = LibVlcFactory.factory().log().create();
    MediaPlayerFactory playerFactory = new MediaPlayerFactory(libvlc);
    for (AudioOutput output : playerFactory.getAudioOutputs()) {
      LOG.warn("-----------");
      LOG.warn(output.getName());
      LOG.warn(output.getDescription());
      for (AudioDevice device : output.getDevices()) {
        LOG.warn("\t" + device.getDeviceId());
        LOG.warn("\t" + device.getLongName());
        LOG.warn("\t*********");
      }
    }

    player = playerFactory.newDirectMediaPlayer(this, this);
    player.addMediaPlayerEventListener(this);
    player.playMedia(MEDIA_URL);
  }

  @Override
  protected void initGl() {
    super.initGl();

    glEnable(GL_DEPTH_TEST);
    glEnable(GL_BLEND);
    glEnable(GL_PRIMITIVE_RESTART);
    videoTexture = new Texture(GL_TEXTURE_2D);
    videoTexture.bind();
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    videoTexture.unbind();
    OpenGL.checkError();
    OpenGL.checkError();
    glPrimitiveRestartIndex(Short.MAX_VALUE);
    MatrixStack.MODELVIEW.lookat(new Vector3f(0, 0, 3), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));

    frameBuffer = new FrameBuffer(width / 2, height);
    frameBuffer.getTexture().bind();
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
    frameBuffer.getTexture().unbind();
    OpenGL.checkError();

    // glPolygonMode(GL_FRONT_AND_BACK, GL_POINT);
    coloredProgram = new Program(ExampleResource.SHADERS_COLORED_VS, ExampleResource.SHADERS_COLORED_FS);
    coloredProgram.link();

    textureProgram = new Program(ExampleResource.SHADERS_TEXTURED_VS, ExampleResource.SHADERS_TEXTURED_FS);
    textureProgram.link();
    screenQuad = OpenGL.makeTexturedQuad();
  }

  @Override
  protected void onResize(int width, int height) {
    super.onResize(width, height);
    MatrixStack.PROJECTION.perspective(80f, aspect / 2.0f, 0.01f, 1000.0f);
  }

  @Override
  protected void update() {
    MatrixStack.MODELVIEW.lookat(new Vector3f(0, 0, 0.8f), new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
    if (newFrame) {
      onNewFrame();
    }
    // get event key here
    while (Keyboard.next()) {
      switch (Keyboard.getEventKey()) {
      case Keyboard.KEY_LEFT:
        player.setTime(player.getTime() - 30 * 1000);
        break;
      case Keyboard.KEY_RIGHT:
        player.setTime(player.getTime() + 30 * 1000);
        break;
      case Keyboard.KEY_UP:
        player.setTime(player.getTime() + 5 * 60 * 1000);
        break;
      case Keyboard.KEY_DOWN:
        player.setTime(player.getTime() - 5 * 60 * 1000);
        break;
      }
    }
  }

  protected void onNewFrame() {
    synchronized (videoData) {
      videoData.position(0);
      videoTexture.bind();
      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, videoWidth, videoHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, videoData);
      glGenerateMipmap(GL_TEXTURE_2D);
      videoTexture.unbind();
      newFrame = false;
    }
  }

  @Override
  protected void onDestroy() {

    player.stop();
    player.release();
  }

  public static void main(String[] args) {
    NativeLibrary.addSearchPath("libvlc", "C:\\Program Files\\VideoLAN\\VLC");
    Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);
    new VideoDemo().run();
  }

  // This callback is executed by the VLCJ library whenever new
  // frame data is available. We cannot transfer it to the OpenGL
  // texture here, because the GL calls need to be confined to the
  // main render thread.
  @Override
  public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
    synchronized (videoData) {
      Memory m = nativeBuffers[0];
      ByteBuffer data = m.getByteBuffer(0, videoData.capacity());
      // data.position(0);
      videoData.position(0);
      videoData.put(data);
      videoData.position(0);

      newFrame = true;
    }
  }

  @Override
  public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
    videoData = BufferUtils.createByteBuffer(sourceWidth * sourceHeight * 4);
    videoWidth = sourceWidth;
    videoHeight = sourceHeight;
    videoAspect = (float) videoWidth / (float) videoHeight;
    return new BufferFormat("RGBA", sourceWidth, sourceHeight, //
        new int[] { sourceWidth * 4 },//
        new int[] { sourceHeight });
  }

  @Override
  public void mediaChanged(MediaPlayer mediaPlayer, libvlc_media_t media, String mrl) {
    LOG.warn("Media Changed");
  }

  @Override
  public void opening(MediaPlayer mediaPlayer) {
    LOG.warn("Opening");
  }

  @Override
  public void buffering(MediaPlayer mediaPlayer, float newCache) {
    LOG.warn("Buffering");
  }

  @Override
  public void playing(MediaPlayer mediaPlayer) {
    LOG.warn("Playing");
  }

  @Override
  public void paused(MediaPlayer mediaPlayer) {
    LOG.warn("Paused");
  }

  @Override
  public void stopped(MediaPlayer mediaPlayer) {
    LOG.warn("Stopped");
  }

  @Override
  public void forward(MediaPlayer mediaPlayer) {
    LOG.warn("Foreward");
  }

  @Override
  public void backward(MediaPlayer mediaPlayer) {
    LOG.warn("backward");

  }

  @Override
  public void finished(MediaPlayer mediaPlayer) {
    LOG.warn("finished");
    mediaPlayer.playMedia(MEDIA_URL);
  }

  @Override
  public void timeChanged(MediaPlayer mediaPlayer, long newTime) {
    // LOG.warn("timeChanged");
  }

  @Override
  public void positionChanged(MediaPlayer mediaPlayer, float newPosition) {
    // LOG.warn("positionChanged");
  }

  @Override
  public void seekableChanged(MediaPlayer mediaPlayer, int newSeekable) {
    LOG.warn("seekableChanged");
  }

  @Override
  public void pausableChanged(MediaPlayer mediaPlayer, int newPausable) {
    LOG.warn("pausableChanged");
  }

  @Override
  public void titleChanged(MediaPlayer mediaPlayer, int newTitle) {
    LOG.warn("titleChanged");
  }

  @Override
  public void snapshotTaken(MediaPlayer mediaPlayer, String filename) {
    LOG.warn("snapshotTaken");
  }

  @Override
  public void lengthChanged(MediaPlayer mediaPlayer, long newLength) {
    LOG.warn("lengthChanged");
  }

  @Override
  public void videoOutput(MediaPlayer mediaPlayer, int newCount) {
    LOG.warn("videoOutput");
  }

  @Override
  public void error(MediaPlayer mediaPlayer) {
    LOG.warn("error");
  }

  @Override
  public void mediaMetaChanged(MediaPlayer mediaPlayer, int metaType) {
    LOG.warn("mediaMetaChanged");
  }

  @Override
  public void mediaSubItemAdded(MediaPlayer mediaPlayer, libvlc_media_t subItem) {
    LOG.warn("mediaSubItemAdded");
  }

  @Override
  public void mediaDurationChanged(MediaPlayer mediaPlayer, long newDuration) {
    LOG.warn("mediaDurationChanged");
  }

  @Override
  public void mediaParsedChanged(MediaPlayer mediaPlayer, int newStatus) {
    LOG.warn("mediaParsedChanged");
  }

  @Override
  public void mediaFreed(MediaPlayer mediaPlayer) {
    LOG.warn("mediaFreed");
  }

  @Override
  public void mediaStateChanged(MediaPlayer mediaPlayer, int newState) {
    LOG.warn("mediaStateChanged");
  }

  @Override
  public void newMedia(MediaPlayer mediaPlayer) {
    LOG.warn("newMedia");
  }

  @Override
  public void subItemPlayed(MediaPlayer mediaPlayer, int subItemIndex) {
    LOG.warn("subItemPlayed");
  }

  @Override
  public void subItemFinished(MediaPlayer mediaPlayer, int subItemIndex) {
    LOG.warn("subItemFinished");
  }

  @Override
  public void endOfSubItems(MediaPlayer mediaPlayer) {
    LOG.warn("endOfSubItems");
  }

  @Override
  protected void renderScene() {
    // glViewport(1, 1, width / 2 - 1, height - 1);
    glClearColor(.3f, .3f, .3f, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glEnable(GL_TEXTURE_2D);
    glDisable(GL_BLEND);
    glColor3f(1,1,1);
    MatrixStack mv = MatrixStack.MODELVIEW;
    mv.withPush(()->{
      mv.scale(0.4f);
      OpenGL.makeColorCube();
      coloredProgram.use();
      MatrixStack.bindAll(coloredProgram);
      OpenGL.COLOR_CUBE.bindVertexArray();
      OpenGL.COLOR_CUBE.draw();
    });
    IndexBuffer.unbind();
    VertexArray.unbind();
    Program.clear();
    
    mv.withPush(()->{
      float scale = 1.8f;
      float eyeAspect = aspect / 2f;
      float x = scale;
      float y = scale / (videoAspect / eyeAspect);
      float z = -0.8f;
      float texOffset = 0;
      if (getCurrentEye() != 0) {
        texOffset = 0.5f;
      }

      MatrixStack.bindAllGl();
      glEnable(GL_TEXTURE_2D);
      videoTexture.bind();
      glBegin(GL_TRIANGLE_STRIP);
      glTexCoord2f(0, texOffset + 0.5f);
      glVertex3f(-x, -y, z);
      glTexCoord2f(1, texOffset + 0.5f);
      glVertex3f(x, -y, z);
      glTexCoord2f(0, texOffset);
      glVertex3f(-x, y, z);
      glTexCoord2f(1, texOffset);
      glVertex3f(x, y, z);
      glEnd();
//      MatrixStack.MODELVIEW.scale(new Vector3f(x, y, 1));
//      textureProgram.use();
//      videoTexture.bind();
//      MatrixStack.bindAll(textureProgram);
//      screenQuad.bindVertexArray();
//      screenQuad.ibo.bind();
//      screenQuad.draw();
    });
//    
//
//    
//    glDisable(GL_DEPTH_TEST);
//    glDisable(GL_BLEND);
//    glDisable(GL_PRIMITIVE_RESTART);
//    glUseProgram(0);
//
//    {
////      glTranslatef(0.5f, 0, 0);
//      // glEnable(GL_TEXTURE_2D);
//      glColor4f(1, 0, 0, 1);
//      glBegin(GL_TRIANGLE_STRIP);
//      // glTexCoord2f(0, texOffset + 0.5f);
//      glVertex2f(-x, -y);
//      // glTexCoord2f(1, texOffset + 0.5f);
//      glVertex2f(x, -y);
//      // glTexCoord2f(0, texOffset);
//      glVertex2f(-x, y);
//      // glTexCoord2f(1, texOffset);
//      glVertex2f(x, y);
//      glEnd();
//    }

//    // videoGeometry.ibo.bind();
//    // videoGeometry.draw();
//    // videoGeometry.ibo.unbind();
//    // VertexArray.unbind();
//    videoTexture.unbind();
//    // videoGeometry.destroy();
//    // MatrixStack.MODELVIEW.pop();
//    // MatrixStack.PROJECTION.pop();
  }
}
