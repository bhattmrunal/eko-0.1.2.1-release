//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ekodevices.library;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.IOException;

public class FileHelper {
  private static final String AUDIO_FILE_NAME = "temp_record_audio.wav";
  private static final String ECG_FILE_NAME = "temp_record_ecg.wav";
  private static final String DIR_NAME = "records";

  public FileHelper() {
  }

  public static File getCachedRecordingFile(Context context, boolean removeExist, FileHelper.RecordingType type) {
    if (context == null) {
      return null;
    } else {
      File dir = new File(getTempDir(context));
      if (!dir.exists()) {
        dir.mkdirs();
      }

      String filename = "temp_record_audio.wav";
      if (type == FileHelper.RecordingType.ECG) {
        filename = "temp_record_ecg.wav";
      }

      File createCachedFile = new File(dir, filename);
      if (removeExist && createCachedFile.exists()) {
        createCachedFile.delete();

        try {
          createCachedFile.createNewFile();
        } catch (IOException var7) {
          var7.printStackTrace();
        }
      }

      return createCachedFile;
    }
  }

  private static String getTempDir(Context context) {
    String state = Environment.getExternalStorageState();
    File filesDir;
    if ("mounted".equals(state)) {
      filesDir = context.getExternalFilesDir((String)null);
    } else {
      filesDir = context.getFilesDir();
    }

    return filesDir.getAbsolutePath() + File.separatorChar + "records";
  }

  public static enum RecordingType {
    Audio,
    ECG;

    private RecordingType() {
    }
  }
}
