#version 460

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inColor;

layout(location = 0) out vec3 outColor;

layout(set = 0, binding = 0) uniform BlendColorSet {
    vec3 color;
} uBlendColorSet;

void main() {
    gl_Position = vec4(inPosition, 1.0);
    outColor = inColor * uBlendColorSet.color;
}
