#version 400 core

in vec4 faceColour;
in vec3 vertexNormal;
in vec3 toLightVector;

out vec4 outColour;

uniform vec3 lightColour;
uniform float useShading;

void main() {
    if (useShading == 1.0) {
        vec3 unitNormal = normalize(vertexNormal);
        vec3 unitLightVector = normalize(toLightVector);

        float product = dot(unitNormal, unitLightVector);
        float brightness = max(product, 0.65);
        vec3 diffuse = brightness * lightColour;

        outColour = vec4(diffuse, 1.0) * faceColour;
    } else {
        outColour = faceColour;
    }
}