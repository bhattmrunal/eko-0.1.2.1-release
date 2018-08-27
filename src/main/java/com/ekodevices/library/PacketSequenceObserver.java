package com.ekodevices.library;

import android.util.Log;







public class PacketSequenceObserver
{
  private final int LAST_SEQUENCE_ID = 15;
  private final String TAG = PacketSequenceObserver.class.getName();
  
  private boolean mIsStartPacket;
  private int mCurrentPacketSequence;
  
  public PacketSequenceObserver()
  {
    mIsStartPacket = true;
  }
  
  public void checkPacketSequence(int packetSequence, IPacketSequenceListener sequenceListener)
  {
    if (mIsStartPacket) {
      mIsStartPacket = false;
      mCurrentPacketSequence = packetSequence;
      sequenceListener.onSequenceCorrect();
      return;
    }
    
    mCurrentPacketSequence = (mCurrentPacketSequence == 15 ? 0 : mCurrentPacketSequence + 1);
    




    if (mCurrentPacketSequence != packetSequence) {
      Log.d(TAG, "invalid sequence");
      resetSavedPacketSequence();
      sequenceListener.onSequenceInvalid();
    } else {
      sequenceListener.onSequenceCorrect();
    }
  }
  
  public void resetSavedPacketSequence() {
    mIsStartPacket = true;
  }
  
  public static abstract interface IPacketSequenceListener
  {
    public abstract void onSequenceCorrect();
    
    public abstract void onSequenceInvalid();
  }
}
