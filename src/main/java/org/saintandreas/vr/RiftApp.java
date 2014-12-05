package org.saintandreas.vr;

import static com.oculusvr.capi.OvrLibrary.ovrDistortionCaps.*;
import static com.oculusvr.capi.OvrLibrary.ovrHmdType.*;
import static com.oculusvr.capi.OvrLibrary.ovrRenderAPIType.*;
import static com.oculusvr.capi.OvrLibrary.ovrTrackingCaps.*;

import java.awt.Rectangle;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.lwjgl.LWJGLUtil;
import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.saintandreas.gl.FrameBuffer;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.app.LwjglApp;
import org.saintandreas.math.Matrix4f;

import com.oculusvr.capi.EyeRenderDesc;
import com.oculusvr.capi.FovPort;
import com.oculusvr.capi.GLTexture;
import com.oculusvr.capi.Hmd;
import com.oculusvr.capi.OvrLibrary;
import com.oculusvr.capi.OvrLibrary.ovrHmdCaps;
import com.oculusvr.capi.OvrVector2i;
import com.oculusvr.capi.OvrVector3f;
import com.oculusvr.capi.Posef;
import com.oculusvr.capi.RenderAPIConfig;
import com.oculusvr.capi.TextureHeader;
import com.sun.jna.Pointer;

public abstract class RiftApp extends LwjglApp {
  protected final Hmd hmd;
  private EyeRenderDesc eyeRenderDescs[] = null;
  private final OvrVector3f eyeOffsets[] =
      (OvrVector3f[])new OvrVector3f().toArray(2);
  private final FovPort fovPorts[] =
      (FovPort[])new FovPort().toArray(2);
  private final GLTexture eyeTextures[] =
      (GLTexture[])new GLTexture().toArray(2);
  protected final Posef[] poses = 
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

      GLTexture texture = eyeTextures[eye];
      TextureHeader header = texture.ogl.Header;
      header.API = ovrRenderAPI_OpenGL;
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

  private static long getNativeWindow() {
    long window = -1;
    try {
      Object displayImpl = null;
      Method[] displayMethods = Display.class.getDeclaredMethods();
      for (Method m : displayMethods) {
        if (m.getName().equals("getImplementation")) {
          m.setAccessible(true);
          displayImpl = m.invoke(null, (Object[]) null);
          break;
        }
      }
      
      String fieldName = null;
      switch (LWJGLUtil.getPlatform()) {
      case LWJGLUtil.PLATFORM_LINUX:
        fieldName = "current_window";
        break;
      case LWJGLUtil.PLATFORM_WINDOWS:
        fieldName = "hwnd";
        break;
      }
      if (null != fieldName) {
        Field[] windowsDisplayFields = displayImpl.getClass().getDeclaredFields();
        for (Field f : windowsDisplayFields) {
          if (f.getName().equals(fieldName)) {
            f.setAccessible(true);
            window = (Long) f.get(displayImpl);
            continue;
          }
        }
      }
    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(e);
    }
    return window;
  }

  @Override
  protected void setupContext() {
    // Bug in LWJGL on OSX returns a 2.1 context if you ask for 3.3, but returns 4.1 if you ask for 3.2
    String osName = System.getProperty("os.name");
    if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
      contextAttributes = new ContextAttribs(3, 2);
    } else {
      contextAttributes = new ContextAttribs(3, 3);
    }
    contextAttributes = contextAttributes
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
      TextureHeader eth = eyeTextures[eye].ogl.Header;
      frameBuffers[eye] = new FrameBuffer(
          eth.TextureSize.w, eth.TextureSize.h);
      eyeTextures[eye].ogl.TexId = frameBuffers[eye].getTexture().id;
    }

    RenderAPIConfig rc = new RenderAPIConfig();
    rc.Header.BackBufferSize = hmd.Resolution;
    rc.Header.Multisample = 1;

    int distortionCaps = 
      ovrDistortionCap_Chromatic |
      ovrDistortionCap_TimeWarp |
      ovrDistortionCap_Vignette;

    for (int i = 0; i < rc.PlatformData.length; ++i) {
      rc.PlatformData[i] = Pointer.createConstant(0);
    }

    eyeRenderDescs = hmd.configureRendering(
        rc, distortionCaps, fovPorts);

    for (int eye = 0; eye < 2; ++eye) {
      this.eyeOffsets[eye].x = eyeRenderDescs[eye].HmdToEyeViewOffset.x;
      this.eyeOffsets[eye].y = eyeRenderDescs[eye].HmdToEyeViewOffset.y;
      this.eyeOffsets[eye].z = eyeRenderDescs[eye].HmdToEyeViewOffset.z;
    }

    // Native window support currently only available on windows
    if (LWJGLUtil.PLATFORM_WINDOWS == LWJGLUtil.getPlatform()) {
      long nativeWindow = getNativeWindow();
      if (0 == (hmd.getEnabledCaps() & ovrHmdCaps.ovrHmdCap_ExtendDesktop)) {
        OvrLibrary.INSTANCE.ovrHmd_AttachToWindow(hmd, Pointer.createConstant(nativeWindow), null, null);
      }
    }
  }

  @Override
  public final void drawFrame() {
    ++frameCount;
    hmd.beginFrame(frameCount);
    Posef eyePoses[] = hmd.getEyePoses(frameCount, eyeOffsets);
    for (int i = 0; i < 2; ++i) {
      int eye = hmd.EyeRenderOrder[i];
      Posef pose = eyePoses[eye];
      MatrixStack.PROJECTION.set(projections[eye]);
      // This doesn't work as it breaks the contiguous nature of the array
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
    // Display update combines both input processing and
    // buffer swapping.  We want only the input processing
    // so we have to call processMessages.
    Display.processMessages();
//    Display.update();
  }

  protected abstract void renderScene();
}
