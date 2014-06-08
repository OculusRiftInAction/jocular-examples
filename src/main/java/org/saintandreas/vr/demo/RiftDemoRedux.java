package org.saintandreas.vr.demo;

import static java.lang.Math.*;
import static org.lwjgl.opengl.GL11.*;

import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.OpenGL;
import org.saintandreas.math.Vector3f;
import org.saintandreas.vr.RiftApp;

public class RiftDemoRedux extends RiftApp {

  @Override
  protected void initGl() {
    super.initGl();
    glEnable(GL_DEPTH_TEST);
    glClearColor(0.2f, 0.2f, 0.2f, 1);

    MatrixStack.MODELVIEW.lookat(
        Vector3f.ZERO,            // eye position
        Vector3f.UNIT_Z.mult(-5), // point to look at
        Vector3f.UNIT_Y);         // up direction

  }

  @Override
  public void renderScene() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    
    for (int x = -5; x <= 5; x++) {
      for (int y = -5; y <= 5; y++) {
        for (int z = -5; z <= 5; z++) {
          if (abs(x * y) == 25
              || abs(y * z) == 25
              || abs(x * z) == 25
              || y == -5) {
            cube(new Vector3f(x, y, z));
          }
        }
      }
    }
  }
  
  private void cube(Vector3f pos) {
    MatrixStack.MODELVIEW.push().translate(pos);
    OpenGL.drawColorCube();
    MatrixStack.MODELVIEW.pop();
  }

  public static void main(String[] args) {
    new RiftDemoRedux().run();
  }
}
