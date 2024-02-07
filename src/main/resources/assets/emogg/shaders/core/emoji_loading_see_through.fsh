#version 150

#moj_import <emogg:emoji_loading_animation.glsl>

uniform vec4 ColorModulator;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = animation(texCoord0) * vertexColor;
    fragColor = color * ColorModulator;
}
