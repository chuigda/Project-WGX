#version 100

precision mediump float;

attribute vec3 inPosition;

void main() {
    gl_Position = vec4(inPosition, 1.0);
}
