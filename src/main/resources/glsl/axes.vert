#version 330 core
uniform mat4 uMVP;
uniform float uLen = 1.0;  // half-length of each axis

out vec3 vColor;

// 6 lines (12 vertices):
// X- : (-1,0,0) -> ( 0,0,0)
// X+ : ( 0,0,0) -> ( 1,0,0)
// Y- : ( 0,-1,0)-> ( 0,0,0)
// Y+ : ( 0,0,0) -> ( 0,1,0)
// Z- : ( 0,0,-1)-> ( 0,0,0)
// Z+ : ( 0,0,0) -> ( 0,0,1)
const vec3 POS[12] = vec3[12](
    vec3(-1, 0, 0), vec3(0, 0, 0),  // X-
    vec3( 0, 0, 0), vec3(1, 0, 0),  // X+
    vec3( 0,-1, 0), vec3(0, 0, 0),  // Y-
    vec3( 0, 0, 0), vec3(0, 1, 0),  // Y+
    vec3( 0, 0,-1), vec3(0, 0, 0),  // Z-
    vec3( 0, 0, 0), vec3(0, 0, 1)   // Z+
);

// Colors: negative halves = black, positive halves = axis color
const vec3 COL[12] = vec3[12](
    vec3(0,0,0), vec3(0,0,0),  // X-
    vec3(1,0,0), vec3(1,0,0),  // X+ (red)
    vec3(0,0,0), vec3(0,0,0),  // Y-
    vec3(0,1,0), vec3(0,1,0),  // Y+ (green)
    vec3(0,0,0), vec3(0,0,0),  // Z-
    vec3(0,0,1), vec3(0,0,1)   // Z+ (blue)
);

void main() {
    vec3 p = POS[gl_VertexID] * uLen;
    vColor = COL[gl_VertexID];
    gl_Position = uMVP * vec4(p, 1.0);
}
