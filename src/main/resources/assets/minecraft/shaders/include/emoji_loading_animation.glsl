#version 150

uniform float animationTime;

#define PI 3.1415926535897932384626433832795

vec4 animation(vec2 uv) {
    vec3 color = vec3(1.0);
    float alpha = 1.0;

    vec2 sweepAxis = normalize(vec2(1.0, 1.0));
    float distToSweep = abs(dot(uv - 0.5, sweepAxis) + sin(animationTime * PI * 2.0) * 1.0);
    color *= smoothstep(0.0, 0.3, distToSweep) * 0.5 + 0.5;

    float radius = distance(uv, vec2(0.5));
    alpha *= smoothstep(0.5, 0.45, radius) * 0.3;

    return vec4(color, alpha);
}
