#version 400 core

out vec4 outColour;

void main() {
    float colour = 81 / 255.0;
    outColour = vec4(colour, colour, colour, 1);
}
