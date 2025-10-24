#version 330 core
layout(location = 0) in vec3 aPosition;
layout(location = 1) in float aWobble;
layout(location = 2) in vec3 aNormal;
layout(location = 3) in vec3 aColor;

uniform mat4 uModel;
uniform mat4 uViewProj;
uniform float uTime;       // optional wobble driver

out vec3 vNormal;
out vec3 vColor;

void main() {
    // simple vertical wobble: scale by attribute (per-vertex) so some verts move more
    float wob = aWobble * sin(uTime);
    vec3 pos = aPosition + vec3(0.0, wob, 0.0);

    gl_Position = uViewProj * uModel * vec4(pos, 1.0);

    // normal (no TBN here; if you scale non-uniformly, use normal matrix)
    mat3 normalMat = mat3(uModel);
    vNormal = normalize(normalMat * aNormal);

    vColor = aColor; // already in [0..1] if authored that way
}

