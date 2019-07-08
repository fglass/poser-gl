#version 400 core

layout (location = 0) in vec2 position;

out float xAxis;
out float zAxis;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void) {
    vec4 worldPosition = transformationMatrix * vec4(position.x, 0.0, position.y, 1.0);
    gl_Position = projectionMatrix * viewMatrix * worldPosition;

    if (position.x == 0) {
        zAxis = 1.0;
    } else if (position.y == 0) {
        xAxis = 1.0;
    } else {
        zAxis = 0.0;
        xAxis = 0.0;
    }
}