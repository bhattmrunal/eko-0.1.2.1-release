package com.ekodevices.library;

class LibEncoder
{
  static final int RANGE_HIGH = 32767;
  static final int RANGE_LOW = -32768;
  private int predictor;
  private int step_index;
  
  LibEncoder() {}
  
  public void initialize() {
    predictor = 0;
    step_index = 0;
  }
  

  private static final short[] ima_index_table = { -1, -1, -1, -1, 2, 4, 6, 8, -1, -1, -1, -1, 2, 4, 6, 8 };
  



  private static final short[] ima_step_table = { 7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21, 23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97, 107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337, 371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, Short.MAX_VALUE };
  










  public byte[] decode(byte[] codes)
  {
    byte[] decodedBytes = new byte[codes.length * 4];
    int i = 0;
    for (byte code : codes) {
      byte[] results = decodeByte(code);
      decodedBytes[(4 * i)] = results[0];
      decodedBytes[(4 * i + 1)] = results[1];
      decodedBytes[(4 * i + 2)] = results[2];
      decodedBytes[(4 * i + 3)] = results[3];
      i++;
    }
    return decodedBytes;
  }
  
  private byte[] decodeByte(byte code) {
    byte[] results = new byte[4];
    
    for (int i : new int[] { 0, 1 }) {
      byte nibbleCode = (byte)(code >> 4 * (1 - i));
      short step = ima_step_table[step_index];
      int diff = step >> 1;
      if ((nibbleCode & 0x4) != 0) {
        diff += (step << 2);
      }
      if ((nibbleCode & 0x2) != 0) {
        diff += (step << 1);
      }
      if ((nibbleCode & 0x1) != 0) {
        diff += step;
      }
      diff >>= 2;
      if ((nibbleCode & 0x8) != 0) {
        predictor -= diff;
      } else {
        predictor += diff;
      }
      
      if (predictor > 32767) {
        predictor = 32767;
      } else if (predictor < 32768) {
        predictor = 32768;
      }
      
      step_index += ima_index_table[(nibbleCode & 0xF)];
      
      if (step_index < 0) {
        step_index = 0;
      } else if (step_index > ima_step_table.length - 1) {
        step_index = (ima_step_table.length - 1);
      }
      
      results[(2 * i + 1)] = ((byte)(predictor >> 8));
      results[(2 * i)] = ((byte)predictor);
    }
    
    return results;
  }
  
  public byte[] encode(int[] vals) {
    byte[] results = new byte[vals.length / 2];
    for (int i = 0; i < vals.length / 2; i++) {
      byte compactCode = 0;
      for (int j : new int[] { 0, 1 }) {
        byte code = encodeShort(vals[(2 * i + j)]);
        compactCode = (byte)(compactCode | (code & 0xF) << 4 * (1 - j));
      }
      results[i] = compactCode;
    }
    return results;
  }
  
  private byte encodeShort(int val) {
    if (val > 32767) {
      val -= 65536;
    }
    

    int step = ima_step_table[step_index];
    int diff = val - predictor;

    short code; if (diff >= 0) {
      code = 0;
    } else {
      code = 8;
      diff = -diff;
    }
    
    int tempStep = step;
    
    if (diff >= tempStep) {
      code = (short)(code | 0x4);
      diff -= tempStep;
    }
    tempStep >>= 1;
    if (diff >= tempStep) {
      code = (short)(code | 0x2);
      diff -= tempStep;
    }
    tempStep >>= 1;
    if (diff >= tempStep) {
      code = (short)(code | 0x1);
    }
    
    long diffq = step >> 3;
    if ((code & 0x4) != 0) {
      diffq += step;
    }
    if ((code & 0x2) != 0) {
      diffq += (step >> 1);
    }
    if ((code & 0x1) != 0) {
      diffq += (step >> 2);
    }
    
    if ((code & 0x8) != 0) {
      predictor = ((int)(predictor - diffq));
    } else {
      predictor = ((int)(predictor + diffq));
    }
    
    if (predictor > 32767) {
      predictor = 32767;
    }
    else if (predictor < 32768) {
      predictor = 32768;
    }
    
    step_index += ima_index_table[code];
    
    if (step_index < 0) {
      step_index = 0;
    }
    else if (step_index > ima_step_table.length - 1) {
      step_index = (ima_step_table.length - 1);
    }
    
    return (byte)(code & 0xF);
  }
}
