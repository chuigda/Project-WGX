#version 460

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoord;

layout(location = 0) out vec2 outTexCoord;

layout(set = 0, binding = 0) uniform ViewProjection {
    mat4 view;
    mat4 proj;
} vp;

layout(push_constant) uniform PushConstantObject {
    mat4 model;
} pco;

void main() {
    gl_Position = vp.proj * vp.view * pco.model * vec4(inPosition, 1.0);
    outTexCoord = inTexCoord;
}
