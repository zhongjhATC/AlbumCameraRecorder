//  顶点坐标
attribute vec4 vPosition;
//  纹理坐标
attribute vec4 vCoord;

uniform mat4 vMatrix;
//  传给片元着色器的像素点
varying vec2 aCoord;

void main() {
    gl_Position = vPosition;
    aCoord = (vMatrix * vec4(vCoord.x, vCoord.y, 1.0, 1.0)).xy;
}