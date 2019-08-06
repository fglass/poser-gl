#version 400 core

layout (location = 0) in vec3 position;

out vec4 passColour;

uniform float isGrid;
uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void) {
    vec4 worldPosition = transformationMatrix * vec4(position.x, position.y, position.z, 1.0);
    gl_Position = projectionMatrix * viewMatrix * worldPosition;

    if (isGrid == 1.0) {
        if (position.x == 0) {
            passColour = vec4(14 / 255.0, 44 / 255.0, 220 / 255.0, 1.0); // Blue
        } else if (position.z == 0) {
            passColour = vec4(120 / 255.0, 10 / 255.0, 10 / 255.0, 1.0); // Red
        } else {
            passColour = vec4(81 / 255.0, 81 / 255.0, 81 / 255.0, 1.0); // Grey
        }
    } else {
        passColour = vec4(221 / 255.0, 215 / 255.0, 0 / 255.0, 1.0); // Yellow
    }
}