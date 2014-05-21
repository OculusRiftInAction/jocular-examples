package org.saintandreas.vr;

import static org.lwjgl.opengl.GL11.*;

import org.saintandreas.ExampleResource;
import org.saintandreas.gl.IndexedGeometry;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.OpenGL;
import org.saintandreas.gl.shaders.Program;
import org.saintandreas.math.Vector3f;
import org.saintandreas.vr.oculus.RiftApp;

public class RiftDemo extends RiftApp {

  private Program program;
  private IndexedGeometry geometry;


  @Override
  protected void initGl() {
    super.initGl();
    MatrixStack.MODELVIEW.lookat(Vector3f.ZERO, // eye position
        Vector3f.UNIT_Z.mult(-1), // origin of the scene
        Vector3f.UNIT_Y); // up direction
    program = new Program(ExampleResource.SHADERS_COLORED_VS, ExampleResource.SHADERS_COLORED_FS);
    program.link();
    geometry = OpenGL.makeColorCube();
  }

  private static final Vector3f AXES[] = { Vector3f.UNIT_X, Vector3f.UNIT_Y, Vector3f.UNIT_Z, };

  @Override
  public void renderScene() {
    glEnable(GL_DEPTH_TEST);
    glClearColor(0.2f, 0.2f, 0.2f, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    program.use();
    MatrixStack.PROJECTION.bind(program);
    MatrixStack mv = MatrixStack.MODELVIEW;
    mv.push();
    geometry.bindVertexArray();
    for (Vector3f axis : AXES) {
      Vector3f offset = axis.mult(ipd * 5);
      mv.push().translate(offset).scale(ipd).bind(program).pop();
      geometry.draw();
      mv.push().translate(offset.mult(-1)).scale(ipd).bind(program).pop();
      geometry.draw();
    }
    mv.pop();
  }

  public static void main(String[] args) {
    new RiftDemo().run();
  }
}
