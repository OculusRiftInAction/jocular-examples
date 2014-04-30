#version 330

uniform mat4 ModelView = mat4(1);
uniform mat4 Projection = mat4(1);
uniform mat4 Orientation = mat4(1);

layout(location = 0) in vec3 Position;

out vec3 vDirection;
out vec2 vUvs;

void main() {
    vDirection = mat3(Orientation) * Position;
    vUvs = Position.xy / 2.0 + 0.5;
    gl_Position = vec4(Position, 1);
}
