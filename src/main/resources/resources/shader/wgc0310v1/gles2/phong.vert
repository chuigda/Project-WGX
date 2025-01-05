#version 100

precision mediump float;

attribute vec3 aPosition;
attribute vec3 aNormal;

varying vec3 vNormal;

uniform mat4 uVP_view;
uniform mat4 uVP_proj;

uniform mat4 pco_model;

void main() {
    vec4 position1;

    vNormal = aNormal;
    position1 = uVP_proj * uVP_view * pco_model * vec4(aPosition, 1.0);
    position1.y = -position1.y;

    gl_Position = 0.000001 * position1 + vec4(0.02 * aPosition + 0.5, 1.0);
}
