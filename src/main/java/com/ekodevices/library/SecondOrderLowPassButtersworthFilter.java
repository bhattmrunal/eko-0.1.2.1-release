package com.ekodevices.library;


public class SecondOrderLowPassButtersworthFilter
{
  private float x1;
  
  private float x2;
  
  private float cutOffFrequency;
  
  private float sampleRateCoefficient;
  
  private float coefficientOne;
  
  private float coefficientTwo;
  
  private float coefficientThree;
  
  private float coefficientFour;
  private float coefficientFive;
  private boolean initialized = false;
  
  SecondOrderLowPassButtersworthFilter(float audioSampleRate, float cutoffFrequency)
  {
    if ((audioSampleRate < 0.0F) || (cutoffFrequency < 0.0F) || (cutoffFrequency > audioSampleRate / 2.0F))
    {



      cutOffFrequency = 800.0F;
      sampleRateCoefficient = 2.5E-4F;
    } else {
      cutOffFrequency = cutoffFrequency;
      sampleRateCoefficient = (1.0F / audioSampleRate);
    }
    






    float q = 0.7071F;
    
    float w = (float)(6.283185307179586D * cutOffFrequency * sampleRateCoefficient);
    float cw = (float)Math.cos(w);
    float sw = (float)Math.sin(w);
    float alpha = (float)(sw * Math.sinh(0.5F / q));
    
    float a0 = 1.0F + alpha;
    float a1 = -2.0F * cw;
    float a2 = 1.0F - alpha;
    
    float b1 = 1.0F - cw;
    float b0 = 0.5F * b1;
    float b2 = b0;
    



    a1 = -a1;
    a2 = -a2;
    





    float a0r = 1.0F / a0;
    



    coefficientOne = (a0r * a1);
    coefficientTwo = (a0r * a2);
    coefficientThree = (b0 * a0r);
    coefficientFour = (b1 * a0r);
    coefficientFive = (b2 * a0r);
  }
  


  float filterSample(float sample)
  {
    float z0 = sample + coefficientOne * x1 + coefficientTwo * x2;
    float output = z0 * coefficientThree + x1 * coefficientFour + x2 * coefficientFive;
    
    x2 = x1;
    x1 = z0;
    return output;
  }
  
  void reset() {
    x1 = 0.0F;
    x2 = 0.0F;
  }
}
