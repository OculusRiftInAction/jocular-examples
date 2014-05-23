package org.saintandreas.vr;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Rectangle;

import org.saintandreas.gl.FrameBuffer;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.app.LwjglApp;
import org.saintandreas.math.Matrix4f;

import com.oculusvr.capi.EyeDesc;
import com.oculusvr.capi.EyeRenderDesc;
import com.oculusvr.capi.FovPort;
import com.oculusvr.capi.GLConfig;
import com.oculusvr.capi.GLTextureData;
import com.oculusvr.capi.HmdDesc;
import com.oculusvr.capi.OvrLibrary;
import com.oculusvr.capi.OvrLibrary.ovrHmd;
import com.oculusvr.capi.Posef;
import com.oculusvr.capi.RenderAPIConfig;
import com.oculusvr.capi.Texture;
import com.oculusvr.capi.Vector2i;

public abstract class RiftApp extends LwjglApp {
  //  private static final OvrLibrary OVR = OvrLibrary.INSTANCE;
  private final ovrHmd hmd;
  private final HmdDesc hmdDesc;

  private EyeRenderDesc eyeRenderDescs[] = (EyeRenderDesc[]) new EyeRenderDesc().toArray(2);
  private Texture eyeTextures[] = (Texture[]) new Texture().toArray(2);
  private FrameBuffer frameBuffers[] = new FrameBuffer[2];
  private int frameCount = -1;
  private Matrix4f projections[] = new Matrix4f[2];

  protected float ipd = OvrLibrary.OVR_DEFAULT_IPD;

  private static ovrHmd openFirstHmd() {
    ovrHmd hmd = ovrHmd.create(0);
    if (null == hmd) {
      hmd = ovrHmd.createDebug(OvrLibrary.ovrHmdType.ovrHmd_DK1);
    }
    return hmd;
  }

  public RiftApp() {
    OvrLibrary.INSTANCE.ovr_Initialize();

    hmd = openFirstHmd();
    if (null == hmd) {
      throw new IllegalStateException("Unable to initialize HMD");
    }

    hmdDesc = hmd.getDesc();
    ipd = hmd.getFloat(OvrLibrary.OVR_KEY_IPD, ipd);
    if (0 == hmd.startSensor(0, 0)) {
      throw new IllegalStateException("Unable to start the sensor");
    }
  }

  @Override
  protected void onDestroy() {
    hmd.destroy();
  }

  @Override
  protected final void setupDisplay() {
    System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
    Rectangle targetRect = new Rectangle(
        hmdDesc.WindowsPos.x, hmdDesc.WindowsPos.y, 
        hmdDesc.Resolution.w, hmdDesc.Resolution.h);
    setupDisplay(targetRect);
  }

  @Override
  protected void initGl() {
    super.initGl();

    EyeDesc eyeDescs[] = (EyeDesc[]) new EyeDesc().toArray(2);
    for (int eye = 0; eye < 2; ++eye) {
      EyeDesc eyeDesc = eyeDescs[eye];
      eyeDesc.Eye = eye;
      {
        FovPort defaultEyeFov = hmdDesc.DefaultEyeFov[eye]; 
        // JNA weirdness 1
        FovPort.ByValue fovPort = new FovPort.ByValue();
        fovPort.DownTan = defaultEyeFov.DownTan;
        fovPort.UpTan = defaultEyeFov.UpTan;
        fovPort.LeftTan = defaultEyeFov.LeftTan;
        fovPort.RightTan = defaultEyeFov.RightTan;
        com.oculusvr.capi.Matrix4f.ByValue projection = 
            OvrLibrary.INSTANCE.ovrMatrix4f_Projection(
                fovPort, 0.1f, 1000000f, (byte) 1);
        projections[eye] = new Matrix4f(projection.M).
            transpose();
        eyeDesc.Fov = fovPort;
        eyeDesc.TextureSize = hmd.getFovTextureSize(
            eye, fovPort, 1.0f);
      }

      eyeDesc.RenderViewport.Size = eyeDesc.TextureSize;
      eyeDesc.RenderViewport.Pos = new Vector2i(0, 0);

      frameBuffers[eye] = new FrameBuffer(
          eyeDesc.TextureSize.w, eyeDesc.TextureSize.h);

      // JNA weirdness to deal with the union type.
      {
        // Create the GLTextureData type pointing to 
        // the same memory as the eyeTexture
        GLTextureData eyeTexture = 
            new GLTextureData(eyeTextures[eye].getPointer());
        eyeTexture.Header.API = 
            OvrLibrary.ovrRenderAPIType.ovrRenderAPI_OpenGL;
        eyeTexture.Header.RenderViewport = eyeDesc.RenderViewport;
        eyeTexture.Header.TextureSize = eyeDesc.TextureSize;
        eyeTexture.TexId = frameBuffers[eye].getTexture().id;
        eyeTexture.write();
      }
    }

    GLConfig rc = new GLConfig();
    rc.Config = new RenderAPIConfig();
    rc.Config.Header.API = 
        OvrLibrary.ovrRenderAPIType.ovrRenderAPI_OpenGL;
    rc.Config.Header.Multisample = 1;
    rc.Config.Header.RTSize = hmdDesc.Resolution;
    int distortionCaps = 
        OvrLibrary.ovrDistortionCaps.ovrDistortion_Chromatic | 
        OvrLibrary.ovrDistortionCaps.ovrDistortion_TimeWarp | 
        OvrLibrary.ovrDistortionCaps.ovrDistortion_Vignette;
    int renderCaps = 0;
    byte configureResult = hmd.configureRendering(
        rc.Config, renderCaps, distortionCaps, 
        eyeDescs, eyeRenderDescs); 

    if (0 == configureResult) {
      throw new IllegalStateException("Unable to configure rendering");
    }
  }


  @Override
  public final void drawFrame() {
    glViewport(0, 0, width, height);
    glClear(GL_COLOR_BUFFER_BIT);
    hmd.beginFrame(++frameCount);
    for (int i = 0; i < 2; ++i) {
      int eye = hmdDesc.EyeRenderOrder[i];
      MatrixStack.PROJECTION.set(projections[eye]);
      Posef.ByValue pose = hmd.beginEyeRender(eye);
      MatrixStack mv = MatrixStack.MODELVIEW;
      mv.push();
      {
        mv.preTranslate(RiftUtils.toVector3f(pose.Position).mult(-1));
        mv.preRotate(RiftUtils.toQuaternion(pose.Orientation).inverse());
        float eyeOffset = (eye == 0 ? 1.0f : -1.0f) * (ipd / 2.0f);
        mv.preTranslate(eyeOffset);
        frameBuffers[eye].activate();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderScene();
        frameBuffers[eye].deactivate();
      }
      mv.pop();
      hmd.endEyeRender(eye, pose, eyeTextures[eye]);
    }
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_CULL_FACE);
    hmd.endFrame();
  }


  protected abstract void renderScene();
}
