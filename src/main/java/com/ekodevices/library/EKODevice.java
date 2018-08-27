//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ekodevices.library;

import java.io.Serializable;

public class EKODevice extends BLEDevice implements Serializable {
  private int volumeLevel;
  private float batteryLevel;

  public EKODevice(String name, String address) {
    super(name, address);
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPeripheralType() {
    if (this.name == null) {
      return "";
    } else {
      String[] parts = this.name.split(" ");
      return parts[0] + " " + parts[1];
    }
  }

  public String getAddress() {
    return this.address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public int getVolumeLevel() {
    return this.volumeLevel;
  }

  public void setVolumeLevel(int volumeLevel) {
    this.volumeLevel = volumeLevel;
  }

  public float getBatteryLevel() {
    return this.batteryLevel;
  }

  public void setBatteryLevel(float batteryLevel) {
    this.batteryLevel = batteryLevel;
  }
}
