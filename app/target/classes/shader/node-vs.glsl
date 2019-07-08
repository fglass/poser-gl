#version 400 core

layout (location = 0) in vec2 position;

uniform mat4 projectionMatrix;
uniform mat4 modelViewMatrix;

void main(void) {
    gl_Position = projectionMatrix * modelViewMatrix * vec4(-position.x, position.y, 0.0, 1.0);
}