#version 100

precision mediump float;

attribute vec3 inPosition;
attribute vec3 inColor;

varying vec3 vColor;

void main() {
    gl_Position = vec4(inPosition, 1.0);
    vColor = inColor;
}
