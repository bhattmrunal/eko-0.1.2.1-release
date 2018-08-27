package com.ekodevices.library;


class SecondOrderHighPassButtersworthFilter
{
  private static float q = 0.7071F;
  
  private float a0r;
  
  private float a1;
  private float a2;
  private float b0;
  private float b1;
  private float b2;
  private float x1 = 0.0F;
  private float x2 = 0.0F;
  
  private boolean initialized = false;
  
  SecondOrderHighPassButtersworthFilter(float audioSampleRate, float cutoffFrequency) {
    if ((audioSampleRate < 0.0F) || (cutoffFrequency < 0.0F) || (cutoffFrequency > audioSampleRate / 2.0F)) {
      initialized = false;
    } else {
      initialized = true;
      float w = 6.2831855F * (cutoffFrequency / audioSampleRate);
      float cw = (float)Math.cos(w);
      float sw = (float)Math.sin(w);
      float alpha = sw * (float)Math.sinh(0.5F / q);
      
      float a0 = 1.0F + alpha;
      a1 = (-2.0F * cw);
      a2 = (1.0F - alpha);
      
      b1 = (-1.0F - cw);
      b0 = (-0.5F * b1);
      b2 = b0;
      
      a1 = (-a1);
      a2 = (-a2);
      
      a0r = (1.0F / a0);
    }
  }
  
  float filterSample(float sample) {
    if (!initialized) {
      return sample;
    }
    
    float z0 = sample + a0r * a1 * x1 + a0r * a2 * x2;
    float output = z0 * b0 * a0r + a0r * b1 * x1 + a0r * b2 * x2;
    x2 = x1;
    x1 = z0;
    return output;
  }
  
  void reset()
  {
    x1 = 0.0F;
    x2 = 0.0F;
  }
}
