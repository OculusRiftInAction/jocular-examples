package org.saintandreas.vr.oculus;

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
  private static final OvrLibrary OVR = OvrLibrary.INSTANCE;
  private final ovrHmd hmd;
  private final HmdDesc hmdDesc;

  private EyeRenderDesc eyeRenderDescs[] = (EyeRenderDesc[]) new EyeRenderDesc().toArray(2);
  private Texture eyeTextures[] = (Texture[]) new Texture().toArray(2);
  private FrameBuffer frameBuffers[] = new FrameBuffer[2];
  private int frameCount = -1;
  private Matrix4f projections[] = new Matrix4f[2];

  protected float ipd = OvrLibrary.OVR_DEFAULT_IPD;

  public RiftApp() {
    OVR.ovr_Initialize();
    ovrHmd hmd = ovrHmd.create(0);
    if (null == hmd) {
      hmd = ovrHmd.createDebug(OvrLibrary.ovrHmdType.ovrHmd_DK1);
    }
    if (null == hmd) {
      throw new IllegalStateException("Unable to initialize HMD");
    }
    this.hmd = hmd;
    hmdDesc = hmd.getDesc();
    ipd = hmd.getFloat(OvrLibrary.OVR_KEY_IPD, ipd);
    if (0 == hmd.startSensor(0, 0)) {
      throw new IllegalStateException("Unable to start the sensor");
    }
  }

  @Override
  protected void initGl() {
    super.initGl();

    EyeDesc eyeDescs[] = (EyeDesc[]) new EyeDesc().toArray(2);
    for (int currentEye = 0; currentEye < 2; ++currentEye) {
      EyeDesc eyeDesc = eyeDescs[currentEye];
      eyeDesc.Eye = currentEye;
      {
        FovPort.ByValue fovPort = new FovPort.ByValue();
        fovPort.DownTan = hmdDesc.DefaultEyeFov[currentEye].DownTan;
        fovPort.UpTan = hmdDesc.DefaultEyeFov[currentEye].UpTan;
        fovPort.LeftTan = hmdDesc.DefaultEyeFov[currentEye].LeftTan;
        fovPort.RightTan = hmdDesc.DefaultEyeFov[currentEye].RightTan;
        projections[currentEye] = new Matrix4f(OVR.ovrMatrix4f_Projection(fovPort, 0.1f, 1000000f, (byte) 1).M).transpose();
        eyeDesc.Fov = fovPort;
        eyeDesc.TextureSize = hmd.getFovTextureSize(currentEye, fovPort, 1.0f);
      }

      eyeDesc.RenderViewport.Size = eyeDesc.TextureSize;
      eyeDesc.RenderViewport.Pos = new Vector2i(0, 0);

      frameBuffers[currentEye] = new FrameBuffer(eyeDesc.TextureSize.w, eyeDesc.TextureSize.h);

      // JNA weirdness to deal with the union type.
      {
        // Create the GLTextureData type pointing to the same memory as the
        // eyeTexture
        GLTextureData eyeTexture = new GLTextureData(eyeTextures[currentEye].getPointer());
        eyeTexture.Header.API = OvrLibrary.ovrRenderAPIType.ovrRenderAPI_OpenGL;
        eyeTexture.Header.RenderViewport = eyeDesc.RenderViewport;
        eyeTexture.Header.TextureSize = eyeDesc.TextureSize;
        eyeTexture.TexId = frameBuffers[currentEye].getTexture().id;
        // Write out the structure to native memory
        eyeTexture.write();
        // Read it back into the other type
        eyeTextures[currentEye].read();
      }
    }

    GLConfig rc = new GLConfig();
    rc.Config = new RenderAPIConfig();
    rc.Config.Header.API = OvrLibrary.ovrRenderAPIType.ovrRenderAPI_OpenGL;
    rc.Config.Header.Multisample = 1;
    rc.Config.Header.RTSize = hmdDesc.Resolution;
    int distortionCaps = OvrLibrary.ovrDistortionCaps.ovrDistortion_Chromatic
        | OvrLibrary.ovrDistortionCaps.ovrDistortion_TimeWarp | OvrLibrary.ovrDistortionCaps.ovrDistortion_Vignette;
    int renderCaps = 0;
    if (0 == hmd.configureRendering(rc.Config, distortionCaps, renderCaps, eyeDescs, eyeRenderDescs)) {
      throw new IllegalStateException("Unable to configure rendering");
    }
  }

  @Override
  public final void drawFrame() {
    glViewport(0, 0, width, height);
    glClear(GL_COLOR_BUFFER_BIT);
    hmd.beginFrame(++frameCount);
    for (int currentEye = 0; currentEye < 2; ++currentEye) {
      MatrixStack.PROJECTION.set(projections[currentEye]);
      Posef.ByValue pose = hmd.beginEyeRender(currentEye);
      MatrixStack m = MatrixStack.MODELVIEW;
      m.push();
      {
        m.preTranslate(pose.Position.toVector3f().mult(-1));
        m.preRotate(pose.Orientation.toQuaternion().inverse());
        float eyeOffset = (currentEye == 0 ? 1.0f : -1.0f) * (ipd / 2.0f);
        m.preTranslate(eyeOffset);
        frameBuffers[currentEye].activate();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        renderScene();
        frameBuffers[currentEye].deactivate();
      }
      m.pop();
      hmd.endEyeRender(currentEye, pose, eyeTextures[currentEye]);
    }
    glDisable(GL_DEPTH_TEST);
    glDisable(GL_CULL_FACE);
    hmd.endFrame();
  }

  @Override
  protected void onDestroy() {
    hmd.destroy();
  }

  @Override
  protected final void setupDisplay() {
    System.setProperty("org.lwjgl.opengl.Window.undecorated", "true");
    Rectangle targetRect = new Rectangle(hmdDesc.WindowsPos.x, hmdDesc.WindowsPos.y, hmdDesc.Resolution.w,
        hmdDesc.Resolution.h);
    setupDisplay(targetRect);
  }

  protected abstract void renderScene();
}
