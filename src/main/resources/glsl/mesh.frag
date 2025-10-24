#version 330 core
in vec3 vNormal;
in vec3 vColor;
out vec4 fragColor;

uniform vec3 uLightDir = normalize(vec3(0.4, 1.0, 0.3));

void main() {
    float ndl = max(dot(normalize(vNormal), normalize(uLightDir)), 0.0);
    vec3 lit = vColor * (0.2 + 0.8 * ndl);
    fragColor = vec4(lit, 1.0);
}

