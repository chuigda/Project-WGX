#version 110

precision mediump float;

attribute vec3 inPosition;
attribute vec3 inColor;

varying vec3 outColor;

void main() {
    gl_Position = vec4(inPosition, 0.0, 1.0);
    outColor = inColor;
}
