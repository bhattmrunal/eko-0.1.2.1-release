package com.ekodevices.library.interfaces;

public abstract interface EkoDeviceOutputDataPoint
{
  public abstract void deviceOutputtedAudioDataPoint(short[] paramArrayOfShort);
  
  public abstract void deviceOutputtedAudio(byte[] paramArrayOfByte);
  
  public abstract void deviceOutputtedECGDataPoint(short[] paramArrayOfShort);
}
