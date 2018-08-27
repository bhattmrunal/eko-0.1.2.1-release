//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ekodevices.library;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

public class BluetoothReceiver extends BroadcastReceiver {
  private BluetoothReceiver.IBluetoothMessageListener mMessageListener;

  public BluetoothReceiver(BluetoothReceiver.IBluetoothMessageListener listener) {
    this.mMessageListener = listener;
  }

  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action != null && !TextUtils.isEmpty(action)) {
      BluetoothDevice device = (BluetoothDevice)intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
      if (device.getName() != null && device.getName().length() >= 8 && device.getAddress() != null) {
        if (this.mMessageListener != null) {
          this.mMessageListener.onBluetoothAction(device, action);
        }

      }
    }
  }

  public interface IBluetoothMessageListener {
    void onBluetoothAction(BluetoothDevice var1, String var2);
  }
}
