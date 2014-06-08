package org.saintandreas.vr.demo;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL31.*;
import static org.saintandreas.ExampleResource.*;

import org.saintandreas.ExampleResource;
import org.saintandreas.gl.IndexedGeometry;
import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.OpenGL;
import org.saintandreas.gl.shaders.Program;
import org.saintandreas.gl.textures.Texture;
import org.saintandreas.math.Quaternion;
import org.saintandreas.math.Vector3f;
import org.saintandreas.resources.Resource;
import org.saintandreas.vr.RiftApp;

import com.oculusvr.capi.OvrLibrary;

public class RiftDemo extends RiftApp {
  private Program cubeProgram;
  private Program skyboxProgram;

  private IndexedGeometry cubeGeometry;
  private Texture skybox;
  private float ipd = OvrLibrary.OVR_DEFAULT_IPD;


  // @formatter:off
  private static final Vector3f AXES[] = { 
    Vector3f.UNIT_X,
    Vector3f.UNIT_Y,
    Vector3f.UNIT_Z,
  };

  private static final Resource SKYBOX[] = {
    IMAGES_SKY_CITY_XNEG_PNG,
    IMAGES_SKY_CITY_XPOS_PNG,
    IMAGES_SKY_CITY_YNEG_PNG,
    IMAGES_SKY_CITY_YPOS_PNG,
    IMAGES_SKY_CITY_ZNEG_PNG,
    IMAGES_SKY_CITY_ZPOS_PNG
  };
  // @formatter:on

  public RiftDemo() {
    ipd = hmd.getFloat(OvrLibrary.OVR_KEY_IPD, ipd);
  }
  
  @Override
  protected void initGl() {
    super.initGl();
    glPrimitiveRestartIndex(Short.MAX_VALUE);
    glEnable(GL_PRIMITIVE_RESTART);

    MatrixStack.MODELVIEW.lookat(Vector3f.ZERO, // eye position
        Vector3f.UNIT_Z.mult(-1), // origin of the scene
        Vector3f.UNIT_Y); // up direction
    cubeProgram = new Program(ExampleResource.SHADERS_COLORED_VS, ExampleResource.SHADERS_COLORED_FS);
    cubeProgram.link();
    skyboxProgram = new Program(SHADERS_CUBEMAP_VS, SHADERS_CUBEMAP_FS);
    skyboxProgram.link();
    cubeGeometry = OpenGL.makeColorCube();
    skybox = OpenGL.getCubemapTextures(SKYBOX);
  }

  @Override
  public void renderScene() {
    glEnable(GL_DEPTH_TEST);
    glClearColor(0.2f, 0.2f, 0.2f, 1);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    cubeGeometry.bindVertexArray();
    MatrixStack mv = MatrixStack.MODELVIEW;

    mv.push();
    {
      Quaternion q = mv.getRotation();
      mv.identity().rotate(q);
      skyboxProgram.use();
      OpenGL.bindAll(skyboxProgram);
      glCullFace(GL_FRONT);
      skybox.bind();
      glDisable(GL_DEPTH_TEST);
      cubeGeometry.draw();
      glEnable(GL_DEPTH_TEST);
      skybox.unbind();
      glCullFace(GL_BACK);
    }
    mv.pop();

    cubeProgram.use();
    OpenGL.bindProjection(cubeProgram);
    cubeGeometry.bindVertexArray();
    for (Vector3f axis : AXES) {
      Vector3f offset = axis.mult(ipd * 8);

      mv.push().translate(offset).scale(ipd);
      OpenGL.bindModelview(cubeProgram);
      cubeGeometry.draw();
      mv.pop();

      mv.push().translate(offset.mult(-1)).scale(ipd);
      OpenGL.bindModelview(cubeProgram);
      cubeGeometry.draw();
      mv.pop();
    }
  }

  public static void main(String[] args) {
    new RiftDemo().run();
  }
}
