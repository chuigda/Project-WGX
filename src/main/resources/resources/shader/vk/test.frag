#version 460

layout(location = 0) in vec4 inColor;
layout(location = 1) flat in int inValue;

layout(location = 0) out vec4 outColor;
layout(location = 1) out int outValue;

void main() {
    outColor = inColor;
    outValue = inValue;
}
