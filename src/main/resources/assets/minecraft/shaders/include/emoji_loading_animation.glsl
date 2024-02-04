#version 150

uniform float animationTime;

#define PI 3.1415926535897932384626433832795

vec4 animation(vec2 uv) {
    vec3 color = vec3(0.0);
    float alpha = 1.0;

    vec2 sweepAxis = normalize(vec2(4.0, 1.0));
    float distToSweep = abs(dot(uv - 0.5, sweepAxis) + animationTime * -2.2 + 1.1);
    color = vec3(smoothstep(0.0, 0.7, distToSweep) * 0.3 + 0.4);

//    color += 0.05 / distToSweep;

    float radius = distance(uv, vec2(0.5));
    alpha *= smoothstep(0.5, 0.45, radius) * 0.5;

    return vec4(color, alpha);
}
