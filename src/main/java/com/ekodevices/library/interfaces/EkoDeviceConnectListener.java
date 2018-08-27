package com.ekodevices.library.interfaces;

import com.ekodevices.library.EKODevice;

public abstract interface EkoDeviceConnectListener
{
  public abstract void connectedToDevice(EKODevice paramEKODevice);
  
  public abstract void failedToConnectToDevice(String paramString, EKODevice paramEKODevice);
  
  public abstract void deviceDidDisconnect(EKODevice paramEKODevice);
}
