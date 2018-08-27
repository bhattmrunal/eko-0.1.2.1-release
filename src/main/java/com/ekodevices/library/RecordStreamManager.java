package com.ekodevices.library;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;











public class RecordStreamManager
{
  private FileOutputStream mAudioFileOutputStream;
  private FileOutputStream mEcgFileOutputStream;
  private Context mContext;
  private LibCore.PeripheralType peripheralType;
  private float mMaxRecordSize;
  private File mCachedAudioRecordingFile;
  private File mCachedEcgRecordingFile;
  private BaseRecordListener mRecordingListener;
  private boolean mIsPaused;
  
  public RecordStreamManager(Context context, LibCore.PeripheralType peripheralType, float recordingTime, BaseRecordListener listener)
  {
    mContext = context;
    mMaxRecordSize = calculateMaxRecordingTime(recordingTime);
    mRecordingListener = listener;
    this.peripheralType = peripheralType;
    try
    {
      prepareRecordStream();
    }
    catch (IOException e) {
      if (mRecordingListener != null) {
        mRecordingListener.onError(e);
      }
      closeStream();
    }
  }
  
  private void prepareRecordStream() throws IOException {
    if (mAudioFileOutputStream != null) {
      mAudioFileOutputStream.close();
    }
    mCachedAudioRecordingFile = FileHelper.getCachedRecordingFile(mContext, true, FileHelper.RecordingType.Audio);
    mAudioFileOutputStream = new FileOutputStream(mCachedAudioRecordingFile);
    
    for (int i = 0; i < 44; i++) {
      mAudioFileOutputStream.write(0);
    }
    
    if (peripheralType == LibCore.PeripheralType.EkoDuo) {
      if (mEcgFileOutputStream != null) {
        mEcgFileOutputStream.close();
      }
      
      mCachedEcgRecordingFile = FileHelper.getCachedRecordingFile(mContext, true, FileHelper.RecordingType.ECG);
      mEcgFileOutputStream = new FileOutputStream(mCachedEcgRecordingFile);
      

      for (int i = 0; i < 44; i++) {
        mEcgFileOutputStream.write(0);
      }
    }
  }
  

  void writeBytes(byte[] audioBytes, byte[] ecgBytes)
  {
    if (mIsPaused) {
      return;
    }
    
    if ((float)mCachedAudioRecordingFile.length() >= mMaxRecordSize) {
      if (mRecordingListener != null) {
        finishProcess();
        mRecordingListener.onRecordTimeEnd(mCachedAudioRecordingFile, mCachedEcgRecordingFile);
      }
      return;
    }
    try
    {
      mAudioFileOutputStream.write(audioBytes);
    }
    catch (Exception localException) {}
    try
    {
      mEcgFileOutputStream.write(ecgBytes);
    }
    catch (Exception localException1) {}
  }
  
  void onDataPacketLost() {
    File existFile = onStop();
    if (mRecordingListener != null) {
      mRecordingListener.onDataLosed(existFile);
    }
  }
  
  private void finishProcess() {
    mIsPaused = true;
    if (mCachedAudioRecordingFile != null) {
      writeWAVHeader(mCachedAudioRecordingFile, FileHelper.RecordingType.Audio);
    }
    if (peripheralType == LibCore.PeripheralType.EkoDuo) {
      writeWAVHeader(mCachedEcgRecordingFile, FileHelper.RecordingType.ECG);
    }
    
    closeStream();
  }
  
  File onStop() {
    finishProcess();
    
    if (mCachedAudioRecordingFile == null) {
      return null;
    }
    
    return mCachedAudioRecordingFile;
  }
  
  private float calculateMaxRecordingTime(float inputTime)
  {
    return 44.0F + inputTime * 8000.0F;
  }
  
  private void closeStream() {
    if (mAudioFileOutputStream != null) {
      try {
        mAudioFileOutputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    
    if (mEcgFileOutputStream != null) {
      try {
        mEcgFileOutputStream.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
  
  boolean isRecordFileValid() {
    return (mCachedAudioRecordingFile != null) && (mCachedAudioRecordingFile.exists()) && (mCachedAudioRecordingFile.length() > 0L) && (mCachedEcgRecordingFile != null) && 
      (mCachedEcgRecordingFile.exists()) && (mCachedEcgRecordingFile.length() > 0L);
  }
  
  private void writeWAVHeader(File file, FileHelper.RecordingType type) {
    try {
      RandomAccessFile mFile = new RandomAccessFile(file, "rw");
      int dataLength = (int)mFile.length() - 44;
      int totalDataLength = dataLength + 36;
      int samplingRate = 4000;
      if (type == FileHelper.RecordingType.ECG) {
        samplingRate = 500;
      }
      int format = 16;
      int channel = 1;
      int bitRate = samplingRate * channel * format;
      
      byte[] header = new byte[44];
      
      header[0] = 82;
      header[1] = 73;
      header[2] = 70;
      header[3] = 70;
      header[4] = ((byte)(totalDataLength & 0xFF));
      header[5] = ((byte)(totalDataLength >> 8 & 0xFF));
      header[6] = ((byte)(totalDataLength >> 16 & 0xFF));
      header[7] = ((byte)(totalDataLength >> 24 & 0xFF));
      header[8] = 87;
      header[9] = 65;
      header[10] = 86;
      header[11] = 69;
      header[12] = 102;
      header[13] = 109;
      header[14] = 116;
      header[15] = 32;
      header[16] = 16;
      header[17] = 0;
      header[18] = 0;
      header[19] = 0;
      header[20] = 1;
      header[21] = 0;
      header[22] = 1;
      header[23] = 0;
      header[24] = ((byte)(samplingRate & 0xFF));
      header[25] = ((byte)(samplingRate >> 8 & 0xFF));
      header[26] = ((byte)(samplingRate >> 16 & 0xFF));
      header[27] = ((byte)(samplingRate >> 24 & 0xFF));
      header[28] = ((byte)(bitRate / 8 & 0xFF));
      header[29] = ((byte)(bitRate / 8 >> 8 & 0xFF));
      header[30] = ((byte)(bitRate / 8 >> 16 & 0xFF));
      header[31] = ((byte)(bitRate / 8 >> 24 & 0xFF));
      header[32] = 2;
      header[33] = 0;
      header[34] = 16;
      header[35] = 0;
      header[36] = 100;
      header[37] = 97;
      header[38] = 116;
      header[39] = 97;
      header[40] = ((byte)(dataLength & 0xFF));
      header[41] = ((byte)(dataLength >> 8 & 0xFF));
      header[42] = ((byte)(dataLength >> 16 & 0xFF));
      header[43] = ((byte)(dataLength >> 24 & 0xFF));
      
      mFile.seek(0L);
      for (byte b : header) {
        mFile.writeByte(b);
      }
      
      mFile.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public static abstract interface BaseRecordListener
  {
    public abstract void onRecordTimeEnd(File paramFile1, File paramFile2);
    
    public abstract void onError(Exception paramException);
    
    public abstract void onDataLosed(File paramFile);
  }
}
