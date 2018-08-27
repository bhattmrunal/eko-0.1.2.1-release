package com.ekodevices.library;

import android.util.Log;

public class Logger {
  public Logger() {}
  
  private static String getClassName(Object object) { String mClassName = object.getClass().getName();
    int mFirstPosition = mClassName.lastIndexOf(".") + 1;
    if (mFirstPosition < 0) {
      mFirstPosition = 0;
    }
    mClassName = mClassName.substring(mFirstPosition);
    mFirstPosition = mClassName.lastIndexOf("$");
    if (mFirstPosition > 0) {
      mClassName = mClassName.substring(0, mFirstPosition);
    }
    
    return mClassName;
  }
  
  public static void e(Object object, String message)
  {
    Log.e((object instanceof String) ? (String)object : getClassName(object), message);
  }
  
  public static void e(Object object, String message, Exception exception)
  {
    Log.e((object instanceof String) ? (String)object : getClassName(object), message, exception);
  }
  
  public static void i(Object object, String message)
  {
    Log.i((object instanceof String) ? (String)object : getClassName(object), message);
  }
  
  public static void v(Object object, String message)
  {
    Log.v((object instanceof String) ? (String)object : getClassName(object), message);
  }
  
  public static void w(Object object, String message)
  {
    Log.w((object instanceof String) ? (String)object : getClassName(object), message);
  }
  
  public static void w(Object object, String message, Exception exception) {}
}
