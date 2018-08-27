package com.ekodevices.library.interfaces;

import com.ekodevices.library.BLEDevice;

public abstract interface EkoDeviceScan
{
  public abstract void foundDevice(BLEDevice paramBLEDevice);
}
