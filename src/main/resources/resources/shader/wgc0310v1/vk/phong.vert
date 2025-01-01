#version 460

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec3 inNormal;

layout(location = 0) out vec3 outFragNormal;

layout(set = 0, binding = 0) uniform UniformBufferObject {
    mat4 view;
    mat4 proj;
} uVP;

layout(push_constant) uniform PushConstant {
    mat4 model;
} pco;

void main() {
    gl_Position = uVP.proj * uVP.view * pco.model * vec4(inPosition, 1.0);
    outFragNormal = inNormal;
}
