package org.saintandreas.vr;

import static com.oculusvr.capi.OvrLibrary.ovrProjectionModifier.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.Rectangle;

import org.lwjgl.opengl.ContextAttribs;
import org.lwjgl.opengl.Display;
import org.saintandreas.gl.FrameBuffer;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.SceneHelpers;
import org.saintandreas.gl.app.LwjglApp;
import org.saintandreas.math.Matrix4f;

import com.oculusvr.capi.EyeRenderDesc;
import com.oculusvr.capi.FovPort;
import com.oculusvr.capi.Hmd;
import com.oculusvr.capi.HmdDesc;
import com.oculusvr.capi.LayerEyeFov;
import com.oculusvr.capi.MirrorTexture;
import com.oculusvr.capi.MirrorTextureDesc;
import com.oculusvr.capi.OvrLibrary;
import com.oculusvr.capi.OvrMatrix4f;
import com.oculusvr.capi.OvrRecti;
import com.oculusvr.capi.OvrSizei;
import com.oculusvr.capi.OvrVector3f;
import com.oculusvr.capi.Posef;
import com.oculusvr.capi.TextureSwapChain;
import com.oculusvr.capi.TextureSwapChainDesc;
import com.oculusvr.capi.ViewScaleDesc;

public abstract class RiftApp extends LwjglApp {
  protected final Hmd hmd;
  protected final HmdDesc hmdDesc;
  private final FovPort[] fovPorts = FovPort.buildPair();
  protected final Posef[] poses = Posef.buildPair();
  private final Matrix4f[] projections = new Matrix4f[2];
  private final OvrVector3f[] eyeOffsets = OvrVector3f.buildPair();
  private final OvrSizei[] textureSizes = new OvrSizei[2];
  private final ViewScaleDesc viewScaleDesc = new ViewScaleDesc();
  private FrameBuffer frameBuffer = null;
  private int frameCount = -1;
  private TextureSwapChain swapChain = null;
  private MirrorTexture mirrorTexture = null;
  private LayerEyeFov layer = new LayerEyeFov();

  public RiftApp() {
    super();
    Hmd.initialize();
    try {
      Thread.sleep(400);
    } catch (InterruptedException e) {
      throw new IllegalStateException(e);
    }

    hmd = Hmd.create();
    if (null == hmd) {
      throw new IllegalStateException("Unable to initialize HMD");
    }
    hmdDesc = hmd.getDesc();

    for (int eye = 0; eye < 2; ++eye) {
      fovPorts[eye] = hmdDesc.DefaultEyeFov[eye];
      OvrMatrix4f m = Hmd.getPerspectiveProjection(fovPorts[eye], 0.1f, 1000000f, ovrProjection_ClipRangeOpenGL);
      projections[eye] = RiftUtils.toMatrix4f(m);
      textureSizes[eye] = hmd.getFovTextureSize(eye, fovPorts[eye], 1.0f);
    }
  }

  @Override
  protected void onDestroy() {
    hmd.destroy();
    Hmd.shutdown();
  }

  @Override
  protected void setupContext() {
    contextAttributes = new ContextAttribs(4, 1).withProfileCore(true).withDebug(true);
  }

  @Override
  protected final void setupDisplay() {
    width = hmdDesc.Resolution.w / 4;
    height = hmdDesc.Resolution.h / 4;
    setupDisplay(new Rectangle(100, 100, width, height));
  }

  @Override
  protected void initGl() {
    super.initGl();
    Display.setVSyncEnabled(false);
    TextureSwapChainDesc desc = new TextureSwapChainDesc();
    desc.Type = OvrLibrary.ovrTextureType.ovrTexture_2D;
    desc.ArraySize = 1;
    desc.Width = textureSizes[0].w + textureSizes[1].w;
    desc.Height = textureSizes[0].h;
    desc.MipLevels = 1;
    desc.Format = OvrLibrary.ovrTextureFormat.OVR_FORMAT_R8G8B8A8_UNORM_SRGB;
    desc.SampleCount = 1;
    desc.StaticImage = false;
    swapChain = hmd.createSwapTextureChain(desc);
    MirrorTextureDesc mirrorDesc = new MirrorTextureDesc();
    mirrorDesc.Format = OvrLibrary.ovrTextureFormat.OVR_FORMAT_R8G8B8A8_UNORM;
    mirrorDesc.Width = width;
    mirrorDesc.Height = height;
    mirrorTexture = hmd.createMirrorTexture(mirrorDesc);

    layer.Header.Type = OvrLibrary.ovrLayerType.ovrLayerType_EyeFov;
    layer.ColorTexure[0] = swapChain;
    layer.Fov = fovPorts;
    layer.RenderPose = poses;
    for (int eye = 0; eye < 2; ++eye) {
      layer.Viewport[eye].Size = textureSizes[eye];
      layer.Viewport[eye].Size = textureSizes[eye];
    }
    layer.Viewport[1].Pos.x = layer.Viewport[1].Size.w;
    frameBuffer = new FrameBuffer(desc.Width, desc.Height);

    for (int eye = 0; eye < 2; ++eye) {
      EyeRenderDesc eyeRenderDesc = hmd.getRenderDesc(eye, fovPorts[eye]);
      this.eyeOffsets[eye].x = eyeRenderDesc.HmdToEyeViewOffset.x;
      this.eyeOffsets[eye].y = eyeRenderDesc.HmdToEyeViewOffset.y;
      this.eyeOffsets[eye].z = eyeRenderDesc.HmdToEyeViewOffset.z;
    }
    viewScaleDesc.HmdSpaceToWorldScaleInMeters = 1.0f;
  }

  @Override
  public final void drawFrame() {
    width = hmdDesc.Resolution.w / 4;
    height = hmdDesc.Resolution.h / 4;

    ++frameCount;
    Posef eyePoses[] = hmd.getEyePoses(frameCount, eyeOffsets);
    frameBuffer.activate();

    MatrixStack pr = MatrixStack.PROJECTION;
    MatrixStack mv = MatrixStack.MODELVIEW;
    int textureId = swapChain.getTextureId(swapChain.getCurrentIndex());
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, textureId, 0);
    for (int eye = 0; eye < 2; ++eye) {
      OvrRecti vp = layer.Viewport[eye];
      glScissor(vp.Pos.x, vp.Pos.y, vp.Size.w, vp.Size.h);
      glViewport(vp.Pos.x, vp.Pos.y, vp.Size.w, vp.Size.h);
      pr.set(projections[eye]);
      Posef pose = eyePoses[eye];
      // This doesn't work as it breaks the contiguous nature of the array
      // FIXME there has to be a better way to do this
      poses[eye].Orientation = pose.Orientation;
      poses[eye].Position = pose.Position;
      mv.push().preTranslate(RiftUtils.toVector3f(poses[eye].Position).mult(-1))
          .preRotate(RiftUtils.toQuaternion(poses[eye].Orientation).inverse());
      renderScene();
      mv.pop();
    }
    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, 0, 0);
    frameBuffer.deactivate();

    swapChain.commit();
    hmd.submitFrame(frameCount, layer);

    // FIXME Copy the layer to the main window using a mirror texture
    glScissor(0, 0, width, height);
    glViewport(0, 0, width, height);
    glClearColor(0.5f, 0.5f, System.currentTimeMillis() % 1000 / 1000.0f, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    SceneHelpers.renderTexturedQuad(mirrorTexture.getTextureId());
  }

  @Override
  protected void finishFrame() {
    // // Display update combines both input processing and
    // // buffer swapping. We want only the input processing
    // // so we have to call processMessages.
    // Display.processMessages();
    Display.update();
  }

  protected abstract void renderScene();
}
