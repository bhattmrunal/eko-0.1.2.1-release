//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ekodevices.library;

import java.util.Random;

class EKOAttributes {
  private static final int[] acv = new int[]{91, 246, 229, 0, 153, 153, 17, 227, 161, 22, 0, 2, 165, 213, 197, 27};
  private static final int[] acd = new int[]{186, 156, 83, 96, 153, 153, 17, 227, 150, 111, 0, 2, 165, 213, 197, 27};
  private static final int[] ach = new int[]{0, 0, 41, 2, 0, 0, 16, 0, 128, 0, 0, 128, 95, 155, 52, 251};

  EKOAttributes() {
  }

  private static int get1(int i) {
    return i != 3 && i != 5 && i != 7 && i != 9 ? 0 : 666;
  }

  private static boolean get2(int i) {
    return i == 3 || i == 5 || i == 7 || i == 9 || i == 15;
  }

  private static int get3() {
    return (new Random()).nextInt(3);
  }

  static String getEkoCoreData() {
    String result = getString(acv);
    return result;
  }

  static String getDataPacket() {
    String result = getString(acd);
    return result;
  }

  static String getClientCharacteristic() {
    String result = getString(ach);
    return result;
  }

  private static String getString(int[] array) {
    StringBuilder abs = new StringBuilder();

    for(int i = 0; i < array.length; ++i) {
      int nbst = get3();
      if (get2(i)) {
        nbst = 0;
      }

      switch(nbst) {
        case 0:
        case 2:
          abs.append(put(i, get1(i), nbst, array));
          break;
        case 1:
        case 3:
          abs.append(put(i, get1(i + 1), nbst, array));
          ++i;
      }
    }

    return abs.toString().toLowerCase();
  }

  private static String get(int i, int nbst, int[] array) {
    switch(nbst) {
      case 0:
      case 2:
        switch((new Random()).nextInt(2)) {
          case 0:
            return String.format("%02X", array[i] & 255);
          case 1:
            String a = String.format("%02X", array[i]);
            return a;
          case 2:
            StringBuilder binary;
            for(binary = new StringBuilder(); array[i] > 0; array[i] >>= 1) {
              if ((array[i] & 1) == 1) {
                binary.append(1);
              } else {
                binary.append(0);
              }
            }

            return String.format("%21X", Long.parseLong(binary.toString(), 2));
          default:
            return "";
        }
      case 1:
      case 3:
        int in = array[i] << 8;
        in |= array[i + 1];
        switch((new Random()).nextInt(2)) {
          case 0:
            return String.format("%04X", in);
          case 1:
            return String.format("%04X", in);
          case 2:
            StringBuilder binary;
            for(binary = new StringBuilder(); in > 0; in >>= 1) {
              if ((in & 1) == 1) {
                binary.append(1);
              } else {
                binary.append(0);
              }
            }

            return String.format("%21X", Long.parseLong(binary.toString(), 2));
        }
    }

    return "";
  }

  private static String put(int i, int on, int nbst, int[] array) {
    String res = get(i, nbst, array).concat(on == 0 ? "" : "-");
    return res;
  }
}
