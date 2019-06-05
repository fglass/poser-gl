#version 400 core

layout (location = 0) in ivec4 position;
layout (location = 1) in ivec3 normal;

out vec4 faceColour;
out vec3 vertexNormal;
out vec3 toLightVector;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform vec3 lightPosition;

void main(void) {
    vec4 worldPosition = transformationMatrix * vec4(position.xyz, 1.0f);
    gl_Position = projectionMatrix * viewMatrix * worldPosition;
    gl_PointSize = 3.0f;

    int ahsl = position.w;
    int hsl = ahsl;
    float a = (ahsl >> 16) / 255.0f;

    // HSB to RGB
    int var5 = hsl / 128;
    float var6 = float(var5 >> 3) / 64.0f + 0.0078125f;
    float var8 = float(var5 & 7) / 8.0f + 0.0625f;

    int var10 = hsl % 128;

    float var11 = float(var10) / 128.0f;
    float r = var11;
    float g = var11;
    float b = var11;

    if (var8 != 0.0f) {
        float var19;
        if (var11 < 0.5f) {
            var19 = var11 * (1.0f + var8);
        } else {
            var19 = var11 + var8 - var11 * var8;
        }

        float var21 = 2.0f * var11 - var19;
        float var23 = var6 + 0.3333333333333333f;
        if (var23 > 1.0f) {
            var23 -= 1.f;
        }

        float var27 = var6 - 0.3333333333333333f;
        if (var27 < 0.0f) {
            var27 += 1.f;
        }

        if (6.0f * var23 < 1.0f) {
            r = var21 + (var19 - var21) * 6.0f * var23;
        } else if (2.0f * var23 < 1.0f) {
            r = var19;
        } else if (3.0f * var23 < 2.0f) {
            r = var21 + (var19 - var21) * (0.6666666666666666f - var23) * 6.0f;
        } else {
            r = var21;
        }

        if (6.0f * var6 < 1.0f) {
            g = var21 + (var19 - var21) * 6.0f * var6;
        } else if (2.0f * var6 < 1.0f) {
            g = var19;
        } else if (3.0f * var6 < 2.0f) {
            g = var21 + (var19 - var21) * (0.6666666666666666f - var6) * 6.0f;
        } else {
            g = var21;
        }

        if (6.0f * var27 < 1.0f) {
            b = var21 + (var19 - var21) * 6.0f * var27;
        } else if (2.0f * var27 < 1.0f) {
            b = var19;
        } else if (3.0f * var27 < 2.0f) {
            b = var21 + (var19 - var21) * (0.6666666666666666f - var27) * 6.0f;
        } else {
            b = var21;
        }
    }

    faceColour = vec4(r, g, b, 1.0f - a);
    vertexNormal = (transformationMatrix * vec4(normal, 0.0f)).xyz;
    toLightVector = lightPosition - worldPosition.xyz;
}