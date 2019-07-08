#version 400 core

out vec4 outColour;

uniform float isHighlighted;

void main(void){
    if (isHighlighted == 1.0) {
        outColour = vec4(221 / 255.0, 0.0, 215 / 255.0, 1.0); // Pink
    } else {
        outColour = vec4(221 / 255.0, 215 / 255.0, 0.0, 1.0); // Yellow
    }
}