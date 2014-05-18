package org.saintandreas.vr;

import static org.lwjgl.opengl.GL11.*;

import org.saintandreas.ExampleResource;
import org.saintandreas.gl.IndexedGeometry;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.OpenGL;
import org.saintandreas.gl.buffers.VertexArray;
import org.saintandreas.gl.shaders.Program;
import org.saintandreas.math.Vector3f;
import org.saintandreas.vr.oculus.RiftApp;

import com.oculusvr.capi.OvrLibrary;


public class RiftDemo extends RiftApp {

  Program program;
  IndexedGeometry geometry;

  @Override
  protected void initGl() {
    super.initGl();
    MatrixStack.MODELVIEW.lookat(Vector3f.UNIT_X.scale(ipd * 5), // eye position
        Vector3f.ZERO, // origin of the scene
        Vector3f.UNIT_Y); // up direction
    program = new Program(ExampleResource.SHADERS_COLORED_VS, ExampleResource.SHADERS_COLORED_FS);
    program.link();
    geometry = OpenGL.makeColorCube();
  }

  @Override
  public void renderScene() {
    glEnable(GL_DEPTH_TEST);
    glClearColor(0.2f, 0.2f, 0.2f, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    MatrixStack mv = MatrixStack.MODELVIEW;
    mv.push();
    {
      mv.scale(OvrLibrary.OVR_DEFAULT_IPD);
      program.use();
      MatrixStack.bindAll(program);
      geometry.bindVertexArray();
      geometry.draw();
      VertexArray.unbind();
      Program.clear();
    }
    mv.pop();
  }

  public static void main(String[] args) {
    new RiftDemo().run();
  }
}
