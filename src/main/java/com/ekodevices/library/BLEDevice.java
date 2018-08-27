
package com.ekodevices.library;

import java.io.Serializable;

public class BLEDevice implements Serializable {
  protected String name;
  protected String address;

  public BLEDevice(String name, String address) {
    this.name = name;
    this.address = address;
  }

  public String getName() {
    return this.name;
  }

  public String getAddress() {
    return this.address;
  }

  public String toString() {
    return "BLEDevice [name=" + this.name + ", address=" + this.address + "]";
  }
}
