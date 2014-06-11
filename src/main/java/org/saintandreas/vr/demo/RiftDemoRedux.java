package org.saintandreas.vr.demo;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.saintandreas.gl.MatrixStack.MODELVIEW;

import org.saintandreas.gl.OpenGL;
import org.saintandreas.math.Vector3f;
import org.saintandreas.vr.RiftApp;

import com.oculusvr.capi.OvrLibrary;

public class RiftDemoRedux extends RiftApp {

  private final float ipd;
  private final float eyeHeight;
  
  public RiftDemoRedux() {
    ipd = hmd.getFloat(OvrLibrary.OVR_KEY_IPD, 
        OvrLibrary.OVR_DEFAULT_IPD);
    eyeHeight = hmd.getFloat(
        OvrLibrary.OVR_KEY_EYE_HEIGHT, 
        OvrLibrary.OVR_DEFAULT_EYE_HEIGHT);
  }
  
  @Override
  protected void initGl() {
    super.initGl();
    glEnable(GL_DEPTH_TEST);
    glClearColor(0.529f, 0.807f, 0.921f, 1); // sky blue

    MODELVIEW.lookat(
        new Vector3f(0, eyeHeight, 0.5f), // eye position
        new Vector3f(0, eyeHeight, 0), // point to look at
        Vector3f.UNIT_Y);         // up direction
  }

  @Override
  public void renderScene() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    Vector3f scale = new Vector3f(
        0.975f, 0.975f, 0.975f);
    
    // Draw arches made of cubes.
    for (int x = -10; x <= 10; x++) {
      for (int y = 0; y <= 10; y++) {
        for (int z = -10; z <= 10; z++) {
          Vector3f pos = new Vector3f(x, y, z);
          float len = pos.length();
          if (len > 11 && len < 12) {
            MODELVIEW.push().translate(pos);
            OpenGL.drawColorCube();
            MODELVIEW.pop();
          }
        }
      }
    }
    
    // Draw a floor made of cubes.
    for (int x = -10; x <= 10; x++) {
      for (int z = -10; z <= 10; z++) {
        Vector3f pos = new Vector3f(x, -0.5f, z);
        MODELVIEW.push().translate(pos).scale(scale);
        OpenGL.drawColorCube();
        MODELVIEW.pop();
      }
    }
    
    // Draw a single cube at the center of the room,
    // at eye height.
    MODELVIEW.push()
        .translate(new Vector3f(0, eyeHeight, 0))
        .scale(ipd);
    OpenGL.drawColorCube();
    MODELVIEW.pop();

    // Put the cube on a pedestal.  (Any point?)
    MODELVIEW.push()
        .translate(new Vector3f(0, eyeHeight / 2, 0))
        .scale(new Vector3f(ipd / 2, eyeHeight, ipd / 2));
    OpenGL.drawColorCube();
    MODELVIEW.pop();
  }

  public static void main(String[] args) {
    new RiftDemoRedux().run();
  }
}
