package org.saintandreas.vr;

import java.awt.Rectangle;

import org.lwjgl.opengl.ContextAttribs;
import org.saintandreas.gl.FrameBuffer;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.app.LwjglApp;
import org.saintandreas.math.Matrix4f;

import com.oculusvr.capi.EyeRenderDesc;
import com.oculusvr.capi.FovPort;
import com.oculusvr.capi.HmdDesc;
import com.oculusvr.capi.OvrLibrary;
import com.oculusvr.capi.OvrLibrary.Hmd;
import com.oculusvr.capi.OvrVector2i;
import com.oculusvr.capi.Posef;
import com.oculusvr.capi.RenderAPIConfig;
import com.oculusvr.capi.Texture;
import com.oculusvr.capi.TextureHeader;

public abstract class RiftApp extends LwjglApp {
  protected final Hmd hmd;
  private final HmdDesc hmdDesc;
  private final EyeRenderDesc eyeRenderDescs[] =
      (EyeRenderDesc[]) new EyeRenderDesc().toArray(2);
  private final FovPort fovPorts[] =
      (FovPort[])new FovPort().toArray(2);

  private final Texture eyeTextures[] =
      new Texture[2];
  private final FrameBuffer frameBuffers[] =
      new FrameBuffer[2];
  private final Matrix4f projections[] =
      new Matrix4f[2];

  private static Hmd openFirstHmd() {
    Hmd hmd = Hmd.create(0);
    if (null == hmd) {
      hmd = Hmd.createDebug(
          OvrLibrary.ovrHmdType.ovrHmd_DK1);
    }
    return hmd;
  }

  public RiftApp() {
    super();

    OvrLibrary.INSTANCE.ovr_Initialize();

    hmd = openFirstHmd();
    if (null == hmd) {
      throw new IllegalStateException(
          "Unable to initialize HMD");
    }

    hmdDesc = hmd.getDesc();
    if (0 == hmd.startSensor(0, 0)) {
      throw new IllegalStateException(
          "Unable to start the sensor");
    }

    for (int eye = 0; eye < 2; ++eye) {
      fovPorts[eye] = hmdDesc.DefaultEyeFov[eye];
      projections[eye] = RiftUtils.toMatrix4f(
          OvrLibrary.INSTANCE.ovrMatrix4f_Projection(
              new FovPort.ByValue(fovPorts[eye]), 0.1f, 10000f, (byte) 1));

      Texture texture = eyeTextures[eye] = new Texture();
      TextureHeader header = texture.Header;
      header.TextureSize = hmd.getFovTextureSize(
          eye, new FovPort.ByValue(fovPorts[eye]), 1.0f);
      header.RenderViewport.Size = header.TextureSize; 
      header.RenderViewport.Pos = new OvrVector2i(0, 0);
      header.API = OvrLibrary.ovrRenderAPIType
          .ovrRenderAPI_OpenGL;
    }
  }

  @Override
  protected void onDestroy() {
    hmd.stopSensor();
    hmd.destroy();
  }

  @Override
  protected final void setupDisplay() {
    contextAttributes = new ContextAttribs(4, 4)
      .withForwardCompatible(true)
      .withProfileCore(true)
      .withDebug(true);
    
    System.setProperty(
        "org.lwjgl.opengl.Window.undecorated", "true");
    
    Rectangle targetRect = new Rectangle(
        hmdDesc.WindowsPos.x, hmdDesc.WindowsPos.y, 
        hmdDesc.Resolution.w, hmdDesc.Resolution.h);
    setupDisplay(targetRect);
  }

  @Override
  protected void initGl() {
    super.initGl();
    for (int eye = 0; eye < 2; ++eye) {
      TextureHeader eth = eyeTextures[eye].Header;
      frameBuffers[eye] = new FrameBuffer(
          eth.TextureSize.w, eth.TextureSize.h);
      eyeTextures[eye].TextureId = 
          frameBuffers[eye].getTexture().id;
    }
    
    RenderAPIConfig rc = new RenderAPIConfig();
    rc.Header.API = OvrLibrary.ovrRenderAPIType.ovrRenderAPI_OpenGL;
    rc.Header.RTSize = hmdDesc.Resolution;
    rc.Header.Multisample = 1;

    int distortionCaps = 
        OvrLibrary.ovrDistortionCaps
          .ovrDistortionCap_Chromatic | 
        OvrLibrary.ovrDistortionCaps
          .ovrDistortionCap_TimeWarp | 
          OvrLibrary.ovrDistortionCaps
          .ovrDistortionCap_NoSwapBuffers | 
        OvrLibrary.ovrDistortionCaps
          .ovrDistortionCap_Vignette;

    byte configureResult = hmd.configureRendering(
        rc, distortionCaps, fovPorts, eyeRenderDescs);
    if (0 == configureResult) {
      throw new IllegalStateException("Unable to configure rendering");
    }
  }


  @Override
  public final void drawFrame() {
    hmd.beginFrame(0);
    for (int i = 0; i < 2; ++i) {
      int eye = hmdDesc.EyeRenderOrder[i];
      MatrixStack.PROJECTION.set(projections[eye]);
      Posef.ByValue pose = hmd.beginEyeRender(eye);
      MatrixStack mv = MatrixStack.MODELVIEW;
      mv.push();
      {
        mv.preTranslate(RiftUtils.toVector3f(pose.Position).mult(-1));
        mv.preRotate(RiftUtils.toQuaternion(pose.Orientation).inverse());
        mv.preTranslate(RiftUtils.toVector3f(eyeRenderDescs[eye].ViewAdjust));
        frameBuffers[eye].activate();
        renderScene();
        frameBuffers[eye].deactivate();
      }
      mv.pop();
      hmd.endEyeRender(eye, pose, eyeTextures[eye]);
    }
    hmd.endFrame();
  }

  protected abstract void renderScene();
}
