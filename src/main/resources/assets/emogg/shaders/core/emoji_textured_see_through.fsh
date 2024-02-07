#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

uniform float grayscale;

in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    color.rgb = mix(color.rgb, vec3(
        color.r * 0.114 +
        color.g * 0.587 +
        color.b * 0.299
    ), grayscale);
    fragColor = color * ColorModulator;
}
