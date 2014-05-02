package org.saintandreas;

import org.saintandreas.gl.MatrixStack;
import org.saintandreas.gl.OpenGL;
import org.saintandreas.gl.shaders.Program;
import org.saintandreas.scene.GeometryNode;
import org.saintandreas.scene.SceneNode;
import org.saintandreas.scene.ShaderNode;
import org.saintandreas.scene.TransformNode;

public class Scene {

  public static SceneNode getColorCube(float scale) {
    Program program = new Program(ExampleResource.SHADERS_COLORED_VS,
        ExampleResource.SHADERS_COLORED_FS);
    return new TransformNode(() -> {
      MatrixStack.MODELVIEW.scale(scale);
    }).addChild(
        new ShaderNode(program, ()-> {
          MatrixStack.bindAll(program);
        }).addChild(
            new GeometryNode(
                OpenGL.makeColorCube()
            )
        )
    );
  }
}
