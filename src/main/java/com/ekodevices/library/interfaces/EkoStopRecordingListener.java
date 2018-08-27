package com.ekodevices.library.interfaces;

import java.io.File;

public abstract interface EkoStopRecordingListener
{
  public abstract void onNoRecordsFound();
  
  public abstract void onWavFileInvalid();
  
  public abstract void onSuccess(File paramFile);
}
