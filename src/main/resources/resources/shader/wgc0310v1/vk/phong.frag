#version 460

layout(location = 0) in vec3 inFragNormal;

layout(location = 0) out vec4 outFragColor;

layout(set = 0, binding = 1) uniform UniformBufferObject {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float shininess;
} uMaterial;

const vec4 light1 = vec4(-30.0, 15.0, 10.0, 1.0);
const vec4 light2 = vec4(30.0, 15.0, 10.0, 1.0);

void main() {
//    vec3 normal = normalize(inFragNormal);
//    vec3 lightDir1 = normalize(light1.xyz);
//    vec3 lightDir2 = normalize(light2.xyz);
//    vec3 viewDir = normalize(-gl_FragCoord.xyz);
//    vec3 reflectDir1 = reflect(-lightDir1, normal);
//    vec3 reflectDir2 = reflect(-lightDir2, normal);
//
//    float ambientStrength = 0.1;
//    vec4 ambient = ambientStrength * uMaterial.ambient;
//
//    float diff1 = max(dot(normal, lightDir1), 0.0);
//    vec4 diffuse1 = diff1 * uMaterial.diffuse;
//
//    float diff2 = max(dot(normal, lightDir2), 0.0);
//    vec4 diffuse2 = diff2 * uMaterial.diffuse;
//
//    float spec1 = pow(max(dot(viewDir, reflectDir1), 0.0), uMaterial.shininess);
//    vec4 specular1 = spec1 * uMaterial.specular;
//
//    float spec2 = pow(max(dot(viewDir, reflectDir2), 0.0), uMaterial.shininess);
//    vec4 specular2 = spec2 * uMaterial.specular;
//
//    outFragColor = ambient + diffuse1 + diffuse2 + specular1 + specular2;

    outFragColor = vec4(inFragNormal, 1.0);
}
