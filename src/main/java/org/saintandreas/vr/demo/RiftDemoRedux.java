package org.saintandreas.vr.demo;

import static java.lang.Math.abs;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL31.GL_PRIMITIVE_RESTART;
import static org.lwjgl.opengl.GL31.glPrimitiveRestartIndex;

import org.saintandreas.ExampleResource;
import org.saintandreas.gl.IndexedGeometry;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.OpenGL;
import org.saintandreas.gl.shaders.Program;
import org.saintandreas.math.Vector3f;
import org.saintandreas.vr.RiftApp;

public class RiftDemoRedux extends RiftApp {

  private Program cubeProgram;
  private IndexedGeometry cubeGeometry;

  @Override
  protected void initGl() {
    super.initGl();
    glPrimitiveRestartIndex(Short.MAX_VALUE);
    glEnable(GL_PRIMITIVE_RESTART);

    MatrixStack.MODELVIEW.lookat(
        Vector3f.ZERO,            // eye position
        Vector3f.UNIT_Z.mult(-5), // point to look at
        Vector3f.UNIT_Y);         // up direction
    cubeProgram = new Program(
        ExampleResource.SHADERS_COLORED_VS,
        ExampleResource.SHADERS_COLORED_FS);
    cubeProgram.link();
    cubeProgram.use();

    cubeGeometry = OpenGL.makeColorCube();
    cubeGeometry.bindVertexArray();

    glEnable(GL_DEPTH_TEST);
    glClearColor(0.2f, 0.2f, 0.2f, 1);
  }

  @Override
  public void renderScene() {
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    MatrixStack.PROJECTION.bind(cubeProgram);
    
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
    MatrixStack.MODELVIEW
        .push()
        .translate(pos)
        .bind(cubeProgram)
        .pop();
    cubeGeometry.draw();
  }

  public static void main(String[] args) {
    new RiftDemoRedux().run();
  }
}
