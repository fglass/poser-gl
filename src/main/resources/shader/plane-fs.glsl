#version 400 core

in float xAxis;
in float zAxis;
out vec4 outColour;

void main() {
    if (zAxis == 1.0) {
        outColour = vec4(14 / 255.0, 44 / 255.0, 161 / 255.0, 0.9); // Blue
    } else if (xAxis == 1.0) {
        outColour = vec4(140 / 255.0, 14 / 255.0, 14 / 255.0, 0.9); // Red
    } else {
        float colour = 81 / 255.0;
        outColour = vec4(colour, colour, colour, 1); // Grey
    }
}
