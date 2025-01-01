#version 460

layout(location = 0) in vec3 inFragNormal;

layout(location = 0) out vec4 outFragColor;

layout(set = 0, binding = 1) uniform UniformBufferObject {
    vec4 ambient;
    vec4 diffuse;
} uMaterial;

const vec4 light1 = vec4(-100.0, 0.0, 100.0, 1.0);
const vec4 light2 = vec4(100.0, 0.0, 100.0, 1.0);

void main() {
    if (!gl_FrontFacing) {
        outFragColor = vec4(1.0, 0.0, 1.0, 1.0);
        return;
    }

    vec3 normal = normalize(inFragNormal);
    vec3 lightDir1 = normalize(light1.xyz);
    vec3 lightDir2 = normalize(light2.xyz);
    vec3 viewDir = normalize(-gl_FragCoord.xyz);
    vec3 reflectDir1 = reflect(-lightDir1, normal);
    vec3 reflectDir2 = reflect(-lightDir2, normal);

    float ambientStrength = 0.1;
    vec4 ambient = ambientStrength * uMaterial.ambient;

    float diff1 = max(dot(normal, lightDir1), 0.0);
    vec4 diffuse1 = diff1 * uMaterial.diffuse;

    float diff2 = max(dot(normal, lightDir2), 0.0);
    vec4 diffuse2 = diff2 * uMaterial.diffuse;

    outFragColor = ambient + diffuse1 + diffuse2;
}
