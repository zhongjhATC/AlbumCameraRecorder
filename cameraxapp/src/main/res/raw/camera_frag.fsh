#extension GL_OES_EGL_image_external : require
// SurfaceTexture比较特殊
// float数据是什么精度的
precision mediump float;

// 采样点的坐标
varying vec2 aCoord;

// 采样器
uniform samplerExternalOES vTexture;

void main() {
}

