#version 330

in vec4 vColor;
in vec2 vEdge;
out vec4 FragColor;

float edgeFactor() {
    vec2 d = fwidth(vEdge);
    vec2 a3 = abs(smoothstep(vec2(0.0), d * 1.2, vec2(1) - abs(vEdge)));
    return min(a3.x, a3.y);
}

void main() {
    FragColor.rgb = mix(vec3(0.2), vColor.rgb, 1 - edgeFactor());
    FragColor.a = vColor.a;
}
