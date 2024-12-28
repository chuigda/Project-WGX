#version 100

precision mediump float;

attribute vec3 aPosition;
attribute vec2 aTexCoord;

varying vec2 vTexCoord;

uniform mat4 uVP_view;
uniform mat4 uVP_proj;

uniform mat4 pco_model;

void main() {
    gl_Position = uVP_proj * uVP_view * pco_model * vec4(aPosition, 1.0);
    vTexCoord = aTexCoord;
}
