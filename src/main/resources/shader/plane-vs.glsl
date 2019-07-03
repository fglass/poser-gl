#version 400 core

layout (location = 0) in vec2 position;

uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void) {
    vec4 worldPosition = transformationMatrix * vec4(position.x, 0.0, position.y, 1.0);
    gl_Position = projectionMatrix * viewMatrix * worldPosition;
}