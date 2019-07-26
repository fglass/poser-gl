#version 400 core

in float xAxis;
in float zAxis;
out vec4 outColour;

void main() {
    if (zAxis == 1.0) {
        outColour = vec4(14 / 255.0, 44 / 255.0, 220 / 255.0, 1.0); // Blue
    } else if (xAxis == 1.0) {
        outColour = vec4(120 / 255.0, 10 / 255.0, 10 / 255.0, 1.0); // Red
    } else {
        float colour = 81 / 255.0;
        outColour = vec4(colour, colour, colour, 1); // Grey
    }
}
