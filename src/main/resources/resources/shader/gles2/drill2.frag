#version 100

precision mediump float;

varying vec2 vTexCoord;

uniform sampler2D uTexture;

uniform vec4 pco_blendColor;

void main() {
    gl_FragColor = texture2D(uTexture, vTexCoord) * pco_blendColor;
}
