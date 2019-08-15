#version 400 core

layout (location = 0) in vec3 position;

out vec4 passColour;

uniform vec3 colour;
uniform mat4 transformationMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;

void main(void) {
    vec4 worldPosition = transformationMatrix * vec4(position.x, position.y, position.z, 1.0);
    gl_Position = projectionMatrix * viewMatrix * worldPosition;
    passColour = vec4(colour, 1.0);
}