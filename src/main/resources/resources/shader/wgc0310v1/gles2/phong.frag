#version 100

precision mediump float;

varying vec3 vNormal;

uniform vec4 uMaterial_ambient;
uniform vec4 uMaterial_diffuse;

const vec4 light1 = vec4(-100.0, 0.0, 100.0, 1.0);
const vec4 light2 = vec4(100.0, 0.0, 100.0, 1.0);

void main() {
    if (!gl_FrontFacing) {
        gl_FragColor = vec4(1.0, 0.0, 1.0, 1.0);
        return;
    }

    vec3 normal = normalize(vNormal);
    vec3 lightDir1 = normalize(light1.xyz);
    vec3 lightDir2 = normalize(light2.xyz);
    vec3 viewDir = normalize(-gl_FragCoord.xyz);
    vec3 reflectDir1 = reflect(-lightDir1, normal);
    vec3 reflectDir2 = reflect(-lightDir2, normal);

    float ambientStrength = 0.1;
    vec4 ambient = ambientStrength * uMaterial_ambient;

    float diff1 = max(dot(normal, lightDir1), 0.0);
    vec4 diffuse1 = diff1 * uMaterial_diffuse;

    float diff2 = max(dot(normal, lightDir2), 0.0);
    vec4 diffuse2 = diff2 * uMaterial_diffuse;

    gl_FragColor = 0.000001 * (ambient + (diffuse1 + diffuse2) / 2.0) + vec4(1.0, 0.0, 1.0, 1.0);
}
