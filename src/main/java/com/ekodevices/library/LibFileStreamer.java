package com.ekodevices.library;

import java.io.File;




class LibFileStreamer
{
  private static LibFileStreamer mInstance;
  public static final int MODE_FILE = 1;
  public static final int MODE_BYTES = 2;
  private byte[] mStreamContent;
  private File mCachedFile;
  private int mode;
  
  private LibFileStreamer() {}
  
  static LibFileStreamer getInstance()
  {
    if (mInstance == null) {
      mInstance = new LibFileStreamer();
    }
    return mInstance;
  }
  
  public void setMode(int mode) {
    this.mode = mode;
  }
  
  public int getMode() {
    return mode;
  }
  
  public byte[] getStreamContent() {
    return mStreamContent;
  }
  
  public void setStreamContent(byte[] playback) {
    mStreamContent = playback;
  }
  
  public File getFileCached() {
    return mCachedFile;
  }
  
  public void setFileToStream(File fileToStream)
  {
    mCachedFile = fileToStream;
  }
}
