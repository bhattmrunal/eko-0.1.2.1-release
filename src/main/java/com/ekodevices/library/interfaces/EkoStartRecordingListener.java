package com.ekodevices.library.interfaces;

import com.ekodevices.library.RecordStreamManager;

public abstract interface EkoStartRecordingListener
  extends RecordStreamManager.BaseRecordListener
{
  public abstract void onInvalidInputTime();
  
  public abstract void recordedAudioCurrentlyStreamingToDevice();
  
  public abstract void noStreamFromDevice();
}
