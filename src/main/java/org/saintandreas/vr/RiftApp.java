package org.saintandreas.vr;

import static org.lwjgl.opengl.GL11.*;

import java.awt.Rectangle;

import org.saintandreas.gl.FrameBuffer;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.OpenGL;
import org.saintandreas.gl.app.LwjglApp;
import org.saintandreas.gl.buffers.VertexArray;
import org.saintandreas.math.Matrix4f;

import com.oculusvr.capi.EyeRenderDesc;
import com.oculusvr.capi.FovPort;
import com.oculusvr.capi.HmdDesc;
import com.oculusvr.capi.OvrLibrary;
import com.oculusvr.capi.OvrLibrary.ovrHmd;
import com.oculusvr.capi.OvrVector2i;
import com.oculusvr.capi.Posef;
import com.oculusvr.capi.RenderAPIConfig;
import com.oculusvr.capi.Texture;
import com.oculusvr.capi.TextureHeader;

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
    OpenGL.checkError();
    
    FovPort fovPorts[] = (FovPort[])new FovPort().toArray(2);  
    for (int eye = 0; eye < 2; ++eye) {
      {
        // JNA weirdness 1
        FovPort defaultEyeFov = hmdDesc.DefaultEyeFov[eye];
        fovPorts[eye] = defaultEyeFov;
        FovPort.ByValue fovPort = new FovPort.ByValue();
        fovPort.DownTan = defaultEyeFov.DownTan;
        fovPort.UpTan = defaultEyeFov.UpTan;
        fovPort.LeftTan = defaultEyeFov.LeftTan;
        fovPort.RightTan = defaultEyeFov.RightTan;
        projections[eye] = RiftUtils.toMatrix4f(
            OvrLibrary.INSTANCE.ovrMatrix4f_Projection(
                fovPort, 0.1f, 1000000f, (byte) 1));

        TextureHeader eth = eyeTextures[eye].Header;
        eth.TextureSize = hmd.getFovTextureSize(eye, fovPort, 1.0f);
        eth.RenderViewport.Size = eth.TextureSize; 
        eth.RenderViewport.Pos = new OvrVector2i(0, 0);
        eth.API = OvrLibrary.ovrRenderAPIType.ovrRenderAPI_OpenGL;

        frameBuffers[eye] = new FrameBuffer(eth.TextureSize.w, eth.TextureSize.h);
        eyeTextures[eye].TextureId = frameBuffers[eye].getTexture().id;
      }
    }

    
    RenderAPIConfig rc = new RenderAPIConfig();
    rc.Header.API = OvrLibrary.ovrRenderAPIType.ovrRenderAPI_OpenGL;
    rc.Header.RTSize = hmdDesc.Resolution;
    rc.Header.Multisample = 1;
    for (int i = 0; i < rc.PlatformData.length; ++i) {
      rc.PlatformData[i] = 0;
    }
    rc.write();
    int distortionCaps = 0 
        | OvrLibrary.ovrDistortionCaps.ovrDistortionCap_Chromatic 
        | OvrLibrary.ovrDistortionCaps.ovrDistortionCap_TimeWarp 
        | OvrLibrary.ovrDistortionCaps.ovrDistortionCap_Vignette
        ;

    VertexArray.unbind();

    byte configureResult = hmd.configureRendering(
        rc, distortionCaps, fovPorts, eyeRenderDescs);
    // Glew init sometimes causes a GL erorr, so we clear it out here
    glGetError();

    if (0 == configureResult) {
      throw new IllegalStateException("Unable to configure rendering");
    }
  }


  @Override
  public final void drawFrame() {
    glViewport(0, 0, width, height);
    glClearColor(1, 1, 1, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
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
    hmd.endFrame();
  }

  protected abstract void renderScene();
}
