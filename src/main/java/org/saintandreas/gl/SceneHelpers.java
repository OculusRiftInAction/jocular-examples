package org.saintandreas.gl;

import static java.lang.Math.PI;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.saintandreas.ExampleResource.*;
import static org.saintandreas.gl.OpenGL.interleaveConstants;
import static org.saintandreas.gl.OpenGL.makeQuad;
import static org.saintandreas.gl.OpenGL.transformed;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.saintandreas.ExampleResource;
import org.saintandreas.gl.buffers.VertexArray;
import org.saintandreas.gl.shaders.Attribute;
import org.saintandreas.gl.shaders.Program;
import org.saintandreas.gl.textures.Texture;
import org.saintandreas.math.Matrix4f;
import org.saintandreas.math.Quaternion;
import org.saintandreas.math.Vector3f;
import org.saintandreas.math.Vector4f;
import org.saintandreas.resources.Resource;

public class SceneHelpers {
  private static IndexedGeometry cubeGeometry;
  private static IndexedGeometry cubeGeometryWithNormals;
  private static Program cubeProgram;

  private static IndexedGeometry floorGeometry;
  private static Program floorProgram;
  private static Texture floorTexture;

  private static Program skyboxProgram;
  private static Texture skyboxTexture;



  public static void renderFloor() {
    if (null == floorGeometry) {
      float size = 100.0f;
      List<Vector4f> vertices = new ArrayList<>();
      vertices.add(new Vector4f(size, 0, size, 1));
      vertices.add(new Vector4f(size, size, 0, 0));
      vertices.add(new Vector4f(size, 0, -size, 1));
      vertices.add(new Vector4f(size, -size, 0, 0));
      vertices.add(new Vector4f(-size, 0, size, 1));
      vertices.add(new Vector4f(-size, size, 0, 0));
      vertices.add(new Vector4f(-size, 0, -size, 1));
      vertices.add(new Vector4f(-size, -size, 0, 0));
      List<Short> indices = new ArrayList<>();
      indices.add((short) 0); // LL
      indices.add((short) 1); // LR
      indices.add((short) 2); // UL
      indices.add((short) 3); // UR
      IndexedGeometry.Builder builder = new IndexedGeometry.Builder(indices, vertices);
      builder.withDrawType(GL_TRIANGLE_STRIP).withAttribute(Attribute.POSITION).withAttribute(Attribute.TEX);
      floorGeometry = builder.build();
    }
    if (null == floorTexture) {
      try {
        floorTexture = Texture.loadImage(IMAGES_FLOOR_PNG);
      } catch (IOException ex) {
        throw new IllegalStateException("Unable to load floor texture", ex);
      }
      floorTexture.bind();
      floorTexture.parameter(GL_TEXTURE_MAG_FILTER, GL_NEAREST);
      floorTexture.parameter(GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_NEAREST);
      glGenerateMipmap(GL_TEXTURE_2D);
      floorTexture.unbind();
    }
    if (null == floorProgram) {
      floorProgram = new Program(SHADERS_TEXTURED_VS, SHADERS_TEXTURED_FS);
      floorProgram.link();
    }

    floorProgram.use();
    OpenGL.bindAll(floorProgram);
    floorTexture.bind();
    floorGeometry.bindVertexArray();
    floorGeometry.draw();
    Texture.unbind(GL_TEXTURE_2D);
    Program.clear();
    VertexArray.unbind();
  }
  
  private static Vector4f toNormal(Matrix4f m) {
    Vector4f v = m.mult(new Vector4f(0, 0, 0, 1));
    v = new Vector4f(new Vector3f(v.x, v.y, v.z).normalize(), 1);
    return v;
  }

  private static List<Vector4f> makeNormalCubeVertices() {
    List<Vector4f> result = new ArrayList<>(6 * 4 * 2);
    Matrix4f m;
    List<Vector4f> q = makeQuad(1.0f);
    Vector3f side = new Vector3f(0, 0, 0.5f);

    // Front
    m = new Matrix4f().translate(new Vector3f(0, 0, 0.5f));
    result.addAll(interleaveConstants(transformed(q, m), toNormal(m)));

    // Back
    m = new Matrix4f().rotate((float) PI, Vector3f.UNIT_X).translate(side);
    result.addAll(interleaveConstants(transformed(q, m), toNormal(m)));

    // Top
    m = new Matrix4f().rotate((float) PI / -2f, Vector3f.UNIT_X).translate(side);
    result.addAll(interleaveConstants(transformed(q, m), toNormal(m)));
    
    // Bottom
    m = new Matrix4f().rotate((float) PI / 2f, Vector3f.UNIT_X).translate(side);
    result.addAll(interleaveConstants(transformed(q, m), toNormal(m)));

    // Left
    m = new Matrix4f().rotate((float) PI / -2f, Vector3f.UNIT_Y).translate(side);
    result.addAll(interleaveConstants(transformed(q, m), toNormal(m)));

    // Right
    m = new Matrix4f().rotate((float) PI / 2f, Vector3f.UNIT_Y).translate(side);
    result.addAll(interleaveConstants(transformed(q, m), toNormal(m)));
    
    return result;
  }

  public static void renderColorCube() {
    if (null == cubeGeometryWithNormals) {
      List<Vector4f> vertices = makeNormalCubeVertices();
      List<Short> indices = OpenGL.makeColorCubeIndices();
      Geometry.Builder builder = new IndexedGeometry.Builder(indices, vertices)
          .withDrawType(GL_TRIANGLE_STRIP)
          .withAttribute(Attribute.POSITION)
          .withAttribute(Attribute.NORMAL);
      cubeGeometryWithNormals = (IndexedGeometry) builder.build();
    }
    if (null == cubeProgram) {
      cubeProgram = new Program(ExampleResource.SHADERS_COLORCUBE_VS, ExampleResource.SHADERS_COLORCUBE_FS);
      cubeProgram.link();
    }
    glPrimitiveRestartIndex(Short.MAX_VALUE);
    glEnable(GL_PRIMITIVE_RESTART);

    cubeProgram.use();
    OpenGL.bindAll(cubeProgram);
    cubeGeometryWithNormals.bindVertexArray();
    cubeGeometryWithNormals.draw();
    Program.clear();
    VertexArray.unbind();
  }

  // @formatter:off
  private static final Resource SKYBOX[] = {
    IMAGES_SKY_CITY_XNEG_PNG,
    IMAGES_SKY_CITY_XPOS_PNG,
    IMAGES_SKY_CITY_YNEG_PNG,
    IMAGES_SKY_CITY_YPOS_PNG,
    IMAGES_SKY_CITY_ZNEG_PNG,
    IMAGES_SKY_CITY_ZPOS_PNG
  };
  // @formatter:on

  public static void renderSkybox() {
    if (null == cubeGeometry) {
      cubeGeometry = OpenGL.makeColorCube();
    }
    if (null == skyboxProgram) {
      skyboxProgram = new Program(SHADERS_CUBEMAP_VS, SHADERS_CUBEMAP_FS);
      skyboxProgram.link();
    }

    if (null == skyboxTexture) {
      skyboxTexture = OpenGL.getCubemapTextures(SKYBOX);
    }

    MatrixStack mv = MatrixStack.MODELVIEW;
    cubeGeometry.bindVertexArray();
    mv.push();
    {
      Quaternion q = mv.getRotation();
      mv.identity().rotate(q);
      skyboxProgram.use();
      OpenGL.bindAll(skyboxProgram);
      glCullFace(GL_FRONT);
      skyboxTexture.bind();
      glDisable(GL_DEPTH_TEST);
      cubeGeometry.draw();
      glEnable(GL_DEPTH_TEST);
      skyboxTexture.unbind();
      glCullFace(GL_BACK);
    }
    mv.pop();
  }

}
