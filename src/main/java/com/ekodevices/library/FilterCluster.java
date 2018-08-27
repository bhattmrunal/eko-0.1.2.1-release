
package com.ekodevices.library;

class FilterCluster {
  private SecondOrderHighPassButtersworthFilter filter0;
  private SecondOrderHighPassButtersworthFilter filter1;
  private SecondOrderHighPassButtersworthFilter filter2;
  private SecondOrderHighPassButtersworthFilter filter3;
  private SecondOrderLowPassButtersworthFilter filter4;
  private SecondOrderLowPassButtersworthFilter filter5;
  private SecondOrderLowPassButtersworthFilter filter6;
  private SecondOrderLowPassButtersworthFilter filter7;
  private int mAmplitudeScaleFactor = 1;

  private native void createNativeAudioFilters();

  private native void destroyNativeAudioFilters();

  private native void resetNativeAudioFilters();

  private native void setCoefficientArraysForDelayFilter(double[] var1, double[] var2);

  private native double filterValueWithDelayFilter(double var1);

  FilterCluster(float audioSampleRate, float highPassCutoffFrequency, float lowPassCutoffFrequencey) {
    this.createNativeAudioFilters();
    double[] arrayA = new double[]{1.0D};
    double[] arrayB = new double[]{0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 1.0D};
    this.setCoefficientArraysForDelayFilter(arrayA, arrayB);
    this.filter0 = new SecondOrderHighPassButtersworthFilter(audioSampleRate, highPassCutoffFrequency);
    this.filter1 = new SecondOrderHighPassButtersworthFilter(audioSampleRate, highPassCutoffFrequency);
    this.filter2 = new SecondOrderHighPassButtersworthFilter(audioSampleRate, highPassCutoffFrequency);
    this.filter3 = new SecondOrderHighPassButtersworthFilter(audioSampleRate, highPassCutoffFrequency);
    this.filter4 = new SecondOrderLowPassButtersworthFilter(audioSampleRate, lowPassCutoffFrequencey);
    this.filter5 = new SecondOrderLowPassButtersworthFilter(audioSampleRate, lowPassCutoffFrequencey);
    this.filter6 = new SecondOrderLowPassButtersworthFilter(audioSampleRate, lowPassCutoffFrequencey);
    this.filter7 = new SecondOrderLowPassButtersworthFilter(audioSampleRate, lowPassCutoffFrequencey);
  }

  void setAmplitudeScaleFactor(int amplitudeScaleFactor) {
    if (amplitudeScaleFactor > 0) {
      this.mAmplitudeScaleFactor = amplitudeScaleFactor;
    }

  }

  public int getAmplitudeScaleFactor() {
    return this.mAmplitudeScaleFactor;
  }

  void resetFilters() {
    this.resetNativeAudioFilters();
    this.filter0.reset();
    this.filter1.reset();
    this.filter2.reset();
    this.filter3.reset();
    this.filter4.reset();
    this.filter5.reset();
    this.filter6.reset();
    this.filter7.reset();
  }

  private boolean isFilterEnable() {
    return Prefs.getBoolean(BLEConstants.get(BLEConstants.PREFS_FILTER_ENABLE), true);
  }

  int filterSample(int sample) {
    float sourceSample = (float)sample;
    double yDelay = this.filterValueWithDelayFilter((double)sample);
    if (this.isFilterEnable()) {
      float y0 = this.filter0.filterSample((float)yDelay);
      float y1 = this.filter1.filterSample(y0);
      float y2 = this.filter2.filterSample(y1);
      float y3 = this.filter3.filterSample(y2);
      float y4 = this.filter4.filterSample(y3);
      float y5 = this.filter5.filterSample(y4);
      float y6 = this.filter6.filterSample(y5);
      sourceSample = this.filter7.filterSample(y6);
    } else {
      sourceSample = (float)yDelay;
    }

    return (int)(sourceSample * (float)this.mAmplitudeScaleFactor);
  }

  static {
    System.loadLibrary("eko-native-filters");
  }
}
