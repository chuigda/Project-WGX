#version 460

layout(location = 0) in vec2 inTexCoord;

layout(set = 0, binding = 0) uniform BlendColorSet { vec3 color; } uBlendColorSet;
layout(set = 0, binding = 1) uniform sampler2D uTexture;

layout(location = 0) out vec4 outColor;

void main() {
    vec4 texColor = texture(uTexture, inTexCoord);
    outColor = vec4(uBlendColorSet.color, 1.0) * texColor;
}
