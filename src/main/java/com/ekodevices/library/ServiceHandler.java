package com.ekodevices.library;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;



public class ServiceHandler
  extends Handler
{
  private IHandlerListener mHandlerListener;
  
  public ServiceHandler(Looper looper, IHandlerListener listener)
  {
    super(looper);
    
    mHandlerListener = listener;
  }
  
  public void handleMessage(Message msg)
  {
    if (mHandlerListener != null) {
      mHandlerListener.onHandleMessage((Intent)msg.obj);
    }
  }
  
  public static abstract interface IHandlerListener
  {
    public abstract void onHandleMessage(Intent paramIntent);
  }
}
