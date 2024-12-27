#version 460

layout(location = 0) in vec2 inTexCoord;
layout(location = 0) out vec4 outColor;

layout(set = 0, binding = 1) uniform sampler2D uTexture;

layout(push_constant) uniform PushConstantObject {
    layout(offset = 64) vec4 blendColor;
} pco;

void main() {
    outColor = texture(uTexture, inTexCoord) * pco.blendColor;
}
