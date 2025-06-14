#version 100
precision mediump float;
uniform sampler2D uTexSampler;
varying vec2 vTexCoord;

void main() {
  vec4 originalColor = texture2D(uTexSampler, vTexCoord);
  vec2 center = vec2(0.5);
  float radius = 0.5;
  float dist = distance(vTexCoord, center);
  float edgeWidth = 0.01;
  float alpha = 1.0 - smoothstep(radius - edgeWidth, radius, dist);
  gl_FragColor = vec4(originalColor.r * alpha, originalColor.g * alpha, originalColor.b * alpha, 1.0);
}