package com.ekodevices.library;

class LibConstants
{
  static boolean bleEnabled = true;
  static final int REQUEST_BLUETOOTH_PERMISSION = 6546;
  static final int REQUEST_PERMISSION = 652;
  static final int BUFFER_SIZE_UPPER = 800;
  static final int BUFFER_SIZE_LOWER = 200;
  static final int WAV_OVERHEAD = 44;
  static final int SCAN_PERIOD = 8000;
  static final int AUDIO_SAMPLE_RATE = 4000;
  static final int HIGH_PASS_CUT_OFF_FREQUENCY = 30;
  static final int LOW_PASS_CUT_OFF_FREQUENCY = 800;
  static final int DEFAULT_AMPLITUDE_SCALE_FACTOR = 1;
  
  LibConstants() {}
}
