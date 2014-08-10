package org.saintandreas.vr;

import static com.oculusvr.capi.OvrLibrary.ovrDistortionCaps.*;
import static com.oculusvr.capi.OvrLibrary.ovrHmdType.*;
import static com.oculusvr.capi.OvrLibrary.ovrTrackingCaps.*;

import java.awt.Rectangle;

import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.saintandreas.gl.FrameBuffer;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.app.LwjglApp;
import org.saintandreas.math.Matrix4f;

import com.oculusvr.capi.EyeRenderDesc;
import com.oculusvr.capi.FovPort;
import com.oculusvr.capi.Hmd;
import com.oculusvr.capi.OvrVector2i;
import com.oculusvr.capi.Posef;
import com.oculusvr.capi.RenderAPIConfig;
import com.oculusvr.capi.Texture;
import com.oculusvr.capi.TextureHeader;

public abstract class RiftApp extends LwjglApp {
  protected final Hmd hmd;
  private EyeRenderDesc eyeRenderDescs[] = null;
  private final FovPort fovPorts[] =
      (FovPort[])new FovPort().toArray(2);
  private final Texture eyeTextures[] =
      (Texture[])new Texture().toArray(2);
  private final Posef[] poses = 
      (Posef[])new Posef().toArray(2);
  private final FrameBuffer frameBuffers[] =
      new FrameBuffer[2];
  private final Matrix4f projections[] =
      new Matrix4f[2];
  private int frameCount = -1;



  private static Hmd openFirstHmd() {
    Hmd hmd = Hmd.create(0);
    if (null == hmd) {
      hmd = Hmd.createDebug(ovrHmd_DK1);
    }
    return hmd;
  }

  public RiftApp() {
    super();

    Hmd.initialize();
    
    try {
      Thread.sleep(400);
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }

    hmd = openFirstHmd();
    if (null == hmd) {
      throw new IllegalStateException(
          "Unable to initialize HMD");
    }

    if (0 == hmd.configureTracking(
        ovrTrackingCap_Orientation | 
        ovrTrackingCap_Position, 0)) {
      throw new IllegalStateException(
          "Unable to start the sensor");
    }

    for (int eye = 0; eye < 2; ++eye) {
      fovPorts[eye] = hmd.DefaultEyeFov[eye];
      projections[eye] = RiftUtils.toMatrix4f(
          Hmd.getPerspectiveProjection(
              fovPorts[eye], 0.1f, 1000000f, true));

      Texture texture = eyeTextures[eye];
      TextureHeader header = texture.Header;
      header.TextureSize = hmd.getFovTextureSize(
          eye, fovPorts[eye], 1.0f);
      header.RenderViewport.Size = header.TextureSize; 
      header.RenderViewport.Pos = new OvrVector2i(0, 0);
    }
  }

  @Override
  protected void onDestroy() {
    hmd.destroy();
    Hmd.shutdown();
  }

  @Override
  protected void setupContext() {
    contextAttributes = new ContextAttribs(3, 3)
    .withForwardCompatible(true)
    .withProfileCore(true)
    .withDebug(true);
  }

  @Override
  protected final void setupDisplay() {
    System.setProperty(
        "org.lwjgl.opengl.Window.undecorated", "true");

    Rectangle targetRect = new Rectangle(
        hmd.WindowsPos.x, hmd.WindowsPos.y, 
        hmd.Resolution.w, hmd.Resolution.h);
    setupDisplay(targetRect);
  }

  @Override
  protected void initGl() {
    super.initGl();
    for (int eye = 0; eye < 2; ++eye) {
      TextureHeader eth = eyeTextures[eye].Header;
      frameBuffers[eye] = new FrameBuffer(
          eth.TextureSize.w, eth.TextureSize.h);
      eyeTextures[eye].TextureId = frameBuffers[eye].getTexture().id;
    }

    RenderAPIConfig rc = new RenderAPIConfig();
    rc.Header.RTSize = hmd.Resolution;
    rc.Header.Multisample = 1;

    int distortionCaps = 
      ovrDistortionCap_Chromatic |
      ovrDistortionCap_TimeWarp |
      ovrDistortionCap_Vignette;

    eyeRenderDescs = hmd.configureRendering(
        rc, distortionCaps, fovPorts);
  }

  @Override
  public final void drawFrame() {
    hmd.beginFrame(++frameCount);
    for (int i = 0; i < 2; ++i) {
      int eye = hmd.EyeRenderOrder[i];
      MatrixStack.PROJECTION.set(projections[eye]);
      // This doesn't work as it breaks the contiguous nature of the array
      Posef pose = hmd.getEyePose(eye);
      // FIXME there has to be a better way to do this
      poses[eye].Orientation = pose.Orientation;
      poses[eye].Position = pose.Position;

      MatrixStack mv = MatrixStack.MODELVIEW;
      mv.push();
      {
        mv.preTranslate(
          RiftUtils.toVector3f(
            poses[eye].Position).mult(-1));
        mv.preRotate(
          RiftUtils.toQuaternion(
            poses[eye].Orientation).inverse());
        mv.preTranslate(
          RiftUtils.toVector3f(
            eyeRenderDescs[eye].ViewAdjust));
        frameBuffers[eye].activate();
        renderScene();
        frameBuffers[eye].deactivate();
      }
      mv.pop();
    }
    hmd.endFrame(poses, eyeTextures);
  }

  @Override
  protected void finishFrame() {
    Display.processMessages();
//    Display.update();
  }

  protected abstract void renderScene();
}
