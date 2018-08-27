//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ekodevices.library;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class BLEUtil {
  BLEUtil() {
  }

  static String parseName(byte[] advertisedData) {
    String name = null;
    if (advertisedData == null) {
      return null;
    } else {
      ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);

      while(buffer.remaining() > 2) {
        byte length = buffer.get();
        if (length == 0) {
          break;
        }

        byte type = buffer.get();
        switch(type) {
          case 9:
            byte[] nameBytes = new byte[length - 1];
            buffer.get(nameBytes);

            try {
              name = new String(nameBytes, "utf-8");
            } catch (UnsupportedEncodingException var7) {
              var7.printStackTrace();
            }
            break;
          default:
            buffer.position(buffer.position() + length - 1);
        }
      }

      return name;
    }
  }

  static int adjustBatteryLevel(int level) {
    int[] adjustedBatteryValue = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 5, 5, 7, 11, 17, 18, 19, 20, 27, 31, 37, 43, 48, 52, 54, 57, 59, 62, 65, 66, 70, 73, 78, 79, 81, 82, 85, 89, 91, 93, 96, 99, 99, 100, 100, 100, 100, 100, 100, 100};
    if (level < 0) {
      level = 0;
    } else if (level > 100) {
      level = 100;
    }

    return adjustedBatteryValue[level];
  }

  static List<UUID> parseUuids(byte[] advertisedData) {
    List<UUID> uuids = new ArrayList();
    ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);

    label28:
    while(buffer.remaining() > 2) {
      byte length = buffer.get();
      if (length == 0) {
        break;
      }

      byte type = buffer.get();
      switch(type) {
        case 2:
        case 3:
          while(true) {
            if (length < 2) {
              continue label28;
            }

            uuids.add(UUID.fromString(String.format("%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
            length = (byte)(length - 2);
          }
        case 4:
        case 5:
        default:
          buffer.position(buffer.position() + length - 1);
          break;
        case 6:
        case 7:
          while(length >= 16) {
            long lsb = buffer.getLong();
            long msb = buffer.getLong();
            uuids.add(new UUID(msb, lsb));
            length = (byte)(length - 16);
          }
      }
    }

    return uuids;
  }
}
