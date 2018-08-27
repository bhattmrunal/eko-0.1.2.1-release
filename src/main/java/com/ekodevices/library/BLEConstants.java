//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ekodevices.library;

import java.util.Arrays;
import java.util.List;

class BLEConstants {
  static final String ERR_PAIRING = "Pairing with device error. Please try again";
  static final String ERR_BT_DISABLED = "Bluetooth Disabled";
  static final String ERR_NOT_SUPPORTED = "Bluetooth Disabled or Not Support BLE";
  static final String EKO_E4_DEVICE_NAME = "Eko Core";
  static final String EKO_E5_DEVICE_NAME = "Eko Duo";
  static final List<String> EKO_DEVICE_NAMES = Arrays.asList("Eko Core", "Eko Duo");
  static final int CONNECT_DELAY = 1500;
  static final int[] TASK_SCAN = new int[]{115, 99, 97, 110, 95, 102, 111, 114, 95, 100, 101, 118, 105, 99, 101};
  static final int[] TASK_STOP_SCAN = new int[]{115, 116, 111, 112, 95, 115, 99, 97, 110};
  static final int[] TASK_CONNECT = new int[]{99, 111, 110, 110, 101, 99, 116};
  static final int[] TASK_RECONNECT = new int[]{114, 101, 99, 111, 110, 110, 101, 99, 116};
  static final int[] TASK_CANCEL_RECONNECT = new int[]{99, 97, 110, 99, 101, 108, 95, 114, 101, 99, 111, 110, 110, 101, 99, 116};
  static final int[] TASK_DISCONNECT = new int[]{100, 105, 115, 99, 111, 110, 110, 101, 99, 116};
  static final int[] TASK_FILTER = new int[]{102, 105, 108, 116, 101, 114};
  static final int[] TASK_CHANGE_AMPLITUDE_SCALE_FACTOR = new int[]{99, 104, 97, 110, 71, 101, 95, 97, 109, 112, 108, 105, 116, 117, 100, 101, 115, 99, 97, 108, 101, 102, 97, 99, 116, 111, 114};
  static final int[] TASK_GET_AMPLITUDE_SCALE_FACTOR = new int[]{116, 97, 115, 107, 103, 95, 101, 116, 95, 97, 109, 112, 108, 105, 116, 117, 100, 101, 115, 99, 97, 108, 101, 102, 97, 99, 116, 111, 114};
  static final int[] TASK_CHECK = new int[]{100, 100, 100, 100, 100, 100, 100, 100};
  static final int[] TASK_RESET_DATA_STREAM = new int[]{114, 101, 115, 101, 116, 95, 115, 116, 114, 101, 97, 109};
  static final int[] TASK_STOP_DATA_STREAM = new int[]{115, 116, 111, 112, 95, 115, 116, 114, 101, 97, 109};
  static final int[] TASK_CHANGE_VOLUME = new int[]{99, 104, 97, 110, 103, 101, 95, 118, 111, 108, 117, 109, 101};
  static final int[] TASK_CLEAR_TASKS = new int[]{99, 108, 101, 97, 114};
  static final int[] TASK_START_STREAMING = new int[]{115, 116, 97, 114, 116, 95, 115, 116, 114, 101, 97, 109, 105, 110, 103};
  static final int[] TASK_STOP_STREAMING = new int[]{115, 116, 111, 112, 95, 115, 116, 114, 101, 97, 109, 105, 110, 103};
  static final int[] TASK_CLOSE = new int[]{99, 108, 111, 115, 101, 95, 103, 97, 116, 116};
  static final int[] TASK_WRITE_STREAM = new int[]{119, 114, 105, 116, 101, 95, 115, 116, 114, 101, 97, 109, 95, 100, 97, 116, 97};
  static final int[] ACTION_CONNECTION_STATUS_CHANGED = new int[]{99, 111, 110, 110, 101, 99, 116, 105, 111, 110, 95, 115, 116, 97, 116, 117, 115, 95, 99, 104, 97, 110, 103, 101, 100};
  static final int[] ACTION_ERROR = new int[]{101, 114, 114, 111, 114, 95, 97, 99, 116, 105, 111, 110};
  static final int[] ACTION_VOLUME_CHANGED = new int[]{118, 111, 108, 117, 109, 101, 95, 99, 104, 97, 110, 103, 101, 100};
  static final int[] ACTION_BATTERY_CHANGED = new int[]{98, 97, 116, 116, 101, 114, 121, 95, 99, 104, 97, 110, 103, 101, 100};
  static final int[] ACTION_STREAM_RESET = new int[]{115, 116, 114, 101, 97, 109, 95, 114, 101, 115, 101, 116};
  static final int[] ACTION_DEVICE_DISCOVERED = new int[]{100, 101, 118, 105, 99, 101, 95, 100, 105, 115, 99, 111, 118, 101, 114, 101, 100};
  static final int[] ACTION_DATA_LOSS = new int[]{100, 97, 116, 97, 108, 111, 115, 101, 100};
  static final int[] ACTION_GET_AMPLITUDE_SCALE_FACTOR = new int[]{97, 99, 116, 105, 111, 110, 95, 103, 101, 116, 95, 97, 109, 112, 108, 105, 116, 117, 100, 101, 115, 99, 97, 108, 101, 102, 97, 99, 116, 111, 114};
  static final int[] EXTRAS_AUDIO_STREAM_DATA = new int[]{115, 116, 114, 101, 97, 109, 95, 100, 97, 116, 97};
  static final int[] EXTRAS_AUDIO_BYTE_DATA = new int[]{98, 121, 116, 101, 100, 97, 116, 97};
  static final int[] EXTRAS_ECG_STREAM_DATA = new int[]{110, 116, 114, 101, 97, 109, 95, 100, 97, 116, 97};
  static final int[] EXTRAS_ECG_BYTE_DATA = new int[]{90, 121, 116, 101, 100, 97, 116, 97};
  static final int[] EXTRAS_DEVICE_NAME = new int[]{100, 101, 118, 105, 99, 101, 95, 110, 97, 109, 101};
  static final int[] EXTRAS_DEVICE_STATUS = new int[]{100, 101, 118, 105, 99, 101, 95, 115, 116, 97, 116, 117, 115};
  static final int[] EXTRAS_DEVICE_ADDRESS = new int[]{100, 101, 118, 105, 99, 101, 95, 97, 100, 100, 114, 101, 115, 115};
  static final int[] EXTRAS_DEVICE_VOLUME = new int[]{100, 101, 118, 105, 99, 101, 95, 118, 111, 108, 117, 109, 101};
  static final int[] EXTRAS_DEVICE_BATTERY = new int[]{100, 101, 118, 105, 99, 101, 95, 98, 97, 116, 116, 101, 114, 121};
  static final int[] EXTRAS_ERROR_MSG = new int[]{101, 114, 114, 111, 114, 95, 109, 101, 115, 115, 97, 103, 101};
  static final int[] EXTRAS_STREAMING_INDEX = new int[]{115, 116, 114, 101, 97, 109, 105, 110, 103, 95, 105, 110, 100, 101, 120};
  static final int[] EXTRAS_AMPLITUDE_SCALE_FACTOR = new int[]{97, 109, 112, 108, 105, 116, 117, 100, 101, 115, 99, 97, 108, 101, 102, 97, 99, 116, 111, 114};
  static final int[] PREFS_FILTER_ENABLE = new int[]{80, 82, 69, 70, 83, 95, 70, 73, 76, 84, 69, 82, 95, 69, 78, 65, 66, 76, 69};
  static final int EKO_NO_DEVICE = 0;
  static final int EKO_E4_DEVICE = 4;
  static final int EKO_E5_DEVICE = 5;
  static final int VolumePacketE4 = 1;
  static final int VolumeBitsE4 = 7;
  static final int BatteryPacketE4 = 0;
  static final int BatteryBitsE4 = 127;
  static final int SequenceNumberPacketE4 = 1;
  static final int SequenceNumberBitsE4 = 120;
  static final int SequenceNumberBitshiftE4 = 3;
  static final int VolumeOrBatteryPacketE5 = 1;
  static final int VolumeOrBatteryBitsE5 = 128;
  static final int BatteryOrVolumeBitshiftE5 = 7;
  static final int VolumePacketE5 = 1;
  static final int VolumeBitsE5 = 127;
  static final int BatteryPacketE5 = 1;
  static final int BatteryBitsE5 = 127;
  static final int SequenceNumberPacketE5 = 0;
  static final int SequenceNumberBitsE5 = 15;

  BLEConstants() {
  }

  static String get(int[] in) {
    StringBuilder sb = new StringBuilder();

    for(int i = 0; i < in.length; ++i) {
      sb.append((char)in[i]);
    }

    return sb.toString();
  }
}
