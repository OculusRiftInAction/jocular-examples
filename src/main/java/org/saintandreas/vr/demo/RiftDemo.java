package org.saintandreas.vr.demo;

import static com.oculusvr.capi.OvrLibrary.*;
import static com.oculusvr.capi.OvrLibrary.ovrHmdCaps.*;

import org.lwjgl.input.Keyboard;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.SceneHelpers;
import org.saintandreas.math.Vector3f;
import org.saintandreas.vr.RiftApp;

import com.oculusvr.capi.OvrLibrary;

public class RiftDemo extends RiftApp {
  
  private final float ipd;
  private final float eyeHeight;

  public RiftDemo() {
    ipd = hmd.getFloat(OvrLibrary.OVR_KEY_IPD, OVR_DEFAULT_IPD);
    eyeHeight = hmd.getFloat(OvrLibrary.OVR_KEY_EYE_HEIGHT, OVR_DEFAULT_EYE_HEIGHT);
    recenterView();
  }

  private void recenterView() {
    Vector3f center = Vector3f.UNIT_Y.mult(eyeHeight);
    Vector3f eye = new Vector3f(0, eyeHeight, ipd * 10.0f);
    MatrixStack.MODELVIEW.lookat(eye, center, Vector3f.UNIT_Y);
    hmd.recenterPose();
  }

  @Override
  protected void onKeyboardEvent() {
    if (0 != hmd.getHSWDisplayState().Displayed) {
      hmd.dismissHSWDisplay();
      return;
    }

    if (!Keyboard.getEventKeyState()) {
      super.onKeyboardEvent();
      return;
    }

    switch (Keyboard.getEventKey()) {
    case Keyboard.KEY_R:
      recenterView();
      break;

    case Keyboard.KEY_P:
      int caps = hmd.getEnabledCaps();
      if (0 != (caps & ovrHmdCap_LowPersistence)) {
        hmd.setEnabledCaps(caps & ~ovrHmdCap_LowPersistence);
      } else {
        hmd.setEnabledCaps(caps | ovrHmdCap_LowPersistence);
      }
      break;

    default:
      super.onKeyboardEvent();
    }
  }

  @Override
  public void renderScene() {
    SceneHelpers.renderSkybox();
    SceneHelpers.renderFloor();

    MatrixStack mv = MatrixStack.MODELVIEW;
    mv.push();
    mv.translate(new Vector3f(0, eyeHeight, 0 )).scale(ipd);
    SceneHelpers.renderColorCube();
    mv.pop();
    mv.push();
    mv.translate(new Vector3f(0, eyeHeight / 2, 0 )).scale(new Vector3f(ipd / 2, eyeHeight, ipd / 2));
    SceneHelpers.renderColorCube();
    mv.pop();
  }

  public static void main(String[] args) {
    new RiftDemo().run();
  }
}
