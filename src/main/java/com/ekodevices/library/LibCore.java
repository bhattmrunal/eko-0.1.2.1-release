package com.ekodevices.library;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.ekodevices.library.interfaces.EkoAmplitudeScaleFactorListener;
import com.ekodevices.library.interfaces.EkoDeviceBatteryLevel;
import com.ekodevices.library.interfaces.EkoDeviceConnectListener;
import com.ekodevices.library.interfaces.EkoDeviceError;
import com.ekodevices.library.interfaces.EkoDeviceOutputDataPoint;
import com.ekodevices.library.interfaces.EkoDeviceScan;
import com.ekodevices.library.interfaces.EkoDeviceVolume;
import com.ekodevices.library.interfaces.EkoGetAmplitudeScaleFactor;
import com.ekodevices.library.interfaces.EkoStartRecordingListener;
import com.ekodevices.library.interfaces.EkoStopRecordingListener;
import java.io.File;



public class LibCore
{
  private static final String TAG = "LibCore";
  
  public static enum PeripheralType
  {
    EkoCore,  EkoDuo,  noPeripheralPaired;
    private PeripheralType() {} } public static int VolumeMaxCore = 6;
  public static int VolumeMaxDuo = 11;
  private static LibCore mInstance;
  
  private LibCore(Context context) { mContext = context;
    Prefs.init(mContext);
    initReceiver();
  }
  
  public static LibCore getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new LibCore(context);
    }
    mInstance.updateContext(context);
    return mInstance;
  }
  

  private Context mContext;
  private BroadcastReceiver mReceiver;
  public void updateContext(Context context)
  {
    mContext = context;
  }
  


  private boolean isScanning = false;
  private boolean isConnecting = false;
  private boolean isStreaming = false;
  private boolean isListening = false;
  private boolean isRecording = false;
  
  private EKODevice mCurrentConnectedDevice;
  
  private EkoDeviceScan mScanListener;
  
  private EkoDeviceConnectListener mConnectListener;
  private EkoDeviceVolume mVolumeListener;
  private EkoDeviceBatteryLevel mBatteryListener;
  private EkoDeviceOutputDataPoint mOutputDataListener;
  private EkoDeviceError mErrorListener;
  private EkoGetAmplitudeScaleFactor mAmplitudeScaleFactorListener;
  private RecordStreamManager mRecordManager;
  private PeripheralType peripheralType;
  
  public boolean isFilterEnable()
  {
    return Prefs.getBoolean(BLEConstants.get(BLEConstants.PREFS_FILTER_ENABLE), true);
  }
  
  public void setFiltering(boolean enable) {
    Prefs.save(BLEConstants.get(BLEConstants.PREFS_FILTER_ENABLE), enable);
    
    Intent filterIntent = new Intent(mContext, BLEService.class);
    filterIntent.setAction(BLEConstants.get(BLEConstants.TASK_FILTER));
    mContext.startService(filterIntent);
  }
  
  public boolean isStreamingToDevice() {
    return isStreaming;
  }
  
  public boolean isReady() {
    return LibConstants.bleEnabled;
  }
  
  public PeripheralType getPeripheralType() {
    if (mCurrentConnectedDevice == null) {
      return PeripheralType.noPeripheralPaired;
    }
    String name = mCurrentConnectedDevice.getName();
    if (name == null)
      return PeripheralType.noPeripheralPaired;
    if (name.contains("Eko Core"))
      return PeripheralType.EkoCore;
    if (name.contains("Eko Duo")) {
      return PeripheralType.EkoDuo;
    }
    return PeripheralType.noPeripheralPaired;
  }
  
  public void startScanningForDevices(EkoDeviceScan scanListener)
  {
    try {
      LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
    }
    catch (Exception localException) {}
    
    IntentFilter filter = new IntentFilter(BLEConstants.get(BLEConstants.ACTION_DEVICE_DISCOVERED));
    LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
    
    mScanListener = scanListener;
    Intent serviceIntent = new Intent(mContext, BLEService.class);
    serviceIntent.setAction(BLEConstants.get(BLEConstants.TASK_SCAN));
    mContext.startService(serviceIntent);
    isScanning = true;
  }
  
  public void checkPermission(Activity activity) {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.M){
      if((activity.checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") != PackageManager.PERMISSION_GRANTED) || (activity.checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) &&
      (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)){
        activity.requestPermissions(new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.WRITE_EXTERNAL_STORAGE"}, 652);
      }
    }
  }
  

  public boolean isSupported()
  {
    boolean isSupported = true;
    if (!mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
      return false;
    }
    BluetoothManager bluetoothManager = (BluetoothManager)mContext.getSystemService(Context.BLUETOOTH_SERVICE);
    BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
    if (bluetoothAdapter == null) {
      return false;
    }
    LibConstants.bleEnabled = isSupported;
    return isSupported;
  }
  
  public void enableBluetooth(Activity activity) {
    if (isBleutoothEnabled(activity)) {
      LibConstants.bleEnabled = true;
    } else {
      LibConstants.bleEnabled = false;
      Intent enableBTIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
      activity.startActivityForResult(enableBTIntent, 6546);
    }
  }
  
  public boolean isBleutoothEnabled(Activity activity) {
    BluetoothManager bluetoothManager = (BluetoothManager)activity.getSystemService(Context.BLUETOOTH_SERVICE);
    BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
    if ((bluetoothAdapter != null) && (!bluetoothAdapter.isEnabled())) {
      return false;
    }
    return true;
  }
  
  public boolean onActivityResult(int requestCode, int resultCode, Intent data)
  {
    switch (requestCode) {
    case 6546: 
      if (resultCode == -1) {
        LibConstants.bleEnabled = true;
      } else {
        LibConstants.bleEnabled = false;
      }
      return LibConstants.bleEnabled;
    }
    
    return false;
  }
  
  private void updateReceiver()
  {
    try
    {
      LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
    }
    catch (Exception localException) {}
    
    IntentFilter filter = new IntentFilter(BLEConstants.get(BLEConstants.ACTION_DEVICE_DISCOVERED));
    filter.addAction(BLEConstants.get(BLEConstants.ACTION_CONNECTION_STATUS_CHANGED));
    filter.addAction(BLEConstants.get(BLEConstants.ACTION_VOLUME_CHANGED));
    filter.addAction(BLEConstants.get(BLEConstants.ACTION_BATTERY_CHANGED));
    filter.addAction(BLEConstants.get(BLEConstants.TASK_WRITE_STREAM));
    LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
  }
  
  public void setOutputDataListener(EkoDeviceOutputDataPoint mOutputDataListener) {
    this.mOutputDataListener = mOutputDataListener;
  }
  
  public void setErrorListener(EkoDeviceError mErrorListener) {
    this.mErrorListener = mErrorListener;
  }
  
  public void setBatteryListener(EkoDeviceBatteryLevel mBatteryListener) {
    updateReceiver();
    this.mBatteryListener = mBatteryListener;
  }
  
  public void setVolumeListener(EkoDeviceVolume volumeListener) {
    updateReceiver();
    mVolumeListener = volumeListener;
  }
  
  public void setConnectListener(EkoDeviceConnectListener connectListener) {
    updateReceiver();
    mConnectListener = connectListener;
  }
  
  public void removeVolumeListener() {
    mVolumeListener = null;
  }
  
  public void removeConnectListener() {
    mConnectListener = null;
  }
  
  public void removeScanListener() {
    mScanListener = null;
  }
  
  public void removeBatteryListener() {
    mBatteryListener = null;
  }
  
  public void removeOutputDataListener() {
    mOutputDataListener = null;
  }
  
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    switch (requestCode) {
    case 652: 
      if (grantResults[0] == 0) {
        LibConstants.bleEnabled = true;
      } else {
        LibConstants.bleEnabled = false;
      }
      break;
    }
  }
  
  public void stopScanningForDevices() {
    mScanListener = null;
    Intent serviceIntent = new Intent(mContext, BLEService.class);
    serviceIntent.setAction(BLEConstants.get(BLEConstants.TASK_STOP_SCAN));
    mContext.startService(serviceIntent);
    isScanning = false;
  }
  
  public boolean isScanningForDevices() {
    return isScanning;
  }
  
  public void connectToDevice(BLEDevice device, EkoDeviceConnectListener connectListener)
  {
    updateReceiver();
    
    mConnectListener = connectListener;
    if (isScanning) {
      stopScanningForDevices();
    }
    if ((device != null) && (device.getAddress() != null)) {
      Log.d("LibCore", "Connecting to device " + device.getAddress());
      Intent serviceIntent = new Intent(mContext, BLEService.class);
      serviceIntent.setAction(BLEConstants.get(BLEConstants.TASK_CONNECT));
      serviceIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_ADDRESS), device.getAddress());
      mContext.startService(serviceIntent);
      isConnecting = true;
    }
    else {
      Intent serviceIntent = new Intent(mContext, BLEService.class);
      serviceIntent.setAction(BLEConstants.get(BLEConstants.TASK_CHECK));
      mContext.startService(serviceIntent);
    }
  }
  
  public void checkConnection(EkoDeviceConnectListener connectListener) {
    updateReceiver();
    
    mConnectListener = connectListener;
    if (isScanning) {
      stopScanningForDevices();
    }
    
    Intent serviceIntent = new Intent(mContext, BLEService.class);
    serviceIntent.setAction(BLEConstants.get(BLEConstants.TASK_CHECK));
    mContext.startService(serviceIntent);
  }
  
  public void cancelDeviceConnection() {
    mConnectListener = null;
    Intent disconnectIntent = new Intent(mContext, BLEService.class);
    disconnectIntent.setAction(BLEConstants.get(BLEConstants.TASK_CANCEL_RECONNECT));
    mContext.startService(disconnectIntent);
    isConnecting = false;
  }
  
  public EKODevice getCurrentConnectedDevice() {
    return mCurrentConnectedDevice;
  }
  
  public boolean isDeviceConnected() {
    return mCurrentConnectedDevice != null;
  }
  
  public boolean isConnectingToDevice() {
    return isConnecting;
  }
  
  public boolean setDeviceVolumeLevel(int volumeLevel, EkoDeviceVolume volumeListener) {
    if (mCurrentConnectedDevice != null) {
      mVolumeListener = volumeListener;
      if (mCurrentConnectedDevice.getVolumeLevel() != volumeLevel) {
        Intent mServiceIntent = new Intent(mContext, BLEService.class);
        mServiceIntent.setAction(BLEConstants.get(BLEConstants.TASK_CHANGE_VOLUME));
        mServiceIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_VOLUME), volumeLevel);
        Log.d("LibCore", "Setting Volume " + String.valueOf(volumeLevel));
        mContext.startService(mServiceIntent);
        updateReceiver();
      }
      return true;
    }
    return false;
  }
  
  public int getVolumeLevel()
  {
    if (mCurrentConnectedDevice == null) {
      return -1;
    }
    return mCurrentConnectedDevice.getVolumeLevel();
  }
  
  public float getBatteryLevel() {
    if (mCurrentConnectedDevice == null) {
      return -1.0F;
    }
    return mCurrentConnectedDevice.getBatteryLevel();
  }
  
  public void disconnect() {
    updateReceiver();
    if (isDeviceConnected()) {
      Intent streamingIntent = new Intent(mContext, BLEService.class);
      streamingIntent.setAction(BLEConstants.get(BLEConstants.TASK_DISCONNECT));
      mContext.startService(streamingIntent);
    }
  }
  
  public void closeConnection() {
    updateReceiver();
    Intent streamingIntent = new Intent(mContext, BLEService.class);
    streamingIntent.setAction(BLEConstants.get(BLEConstants.TASK_CLOSE));
    mContext.startService(streamingIntent);
    mCurrentConnectedDevice = null;
  }
  
  public void clearTasks() {
    updateReceiver();
    Intent streamingIntent = new Intent(mContext, BLEService.class);
    streamingIntent.setAction(BLEConstants.get(BLEConstants.TASK_CLEAR_TASKS));
    mContext.startService(streamingIntent);
  }
  












  public void streamAudioToConnectedDevice(File audioFile, float streamIndex)
  {
    updateReceiver();
    if (isStreaming) {
      return;
    }
    
    if (isRecording) {
      stopRecording(null);
    }
    
    LibFileStreamer.getInstance().setFileToStream(audioFile);
    LibFileStreamer.getInstance().setMode(1);
    Intent streamingIntent = new Intent(mContext, BLEService.class);
    streamingIntent.setAction(BLEConstants.get(BLEConstants.TASK_START_STREAMING));
    streamingIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_STREAMING_INDEX), (int)streamIndex);
    mContext.startService(streamingIntent);
    isStreaming = true;
  }
  
  public void stopStreamingAudioToConnectedDevice() {
    if (!isStreaming) {
      return;
    }
    isStreaming = false;
    Intent streamingIntent = new Intent(mContext, BLEService.class);
    streamingIntent.setAction(BLEConstants.get(BLEConstants.TASK_STOP_STREAMING));
    mContext.startService(streamingIntent);
  }
  
  public boolean isListeningForAStream() {
    return isListening;
  }
  
  public void startStreamingFromDevice(EkoDeviceOutputDataPoint listener) {
    updateReceiver();
    isListening = true;
    mOutputDataListener = listener;
    Intent mServiceIntent = new Intent(mContext, BLEService.class);
    mServiceIntent.setAction(BLEConstants.get(BLEConstants.TASK_RESET_DATA_STREAM));
    mContext.startService(mServiceIntent);
  }
  
  public void stopStreamingFromDevice() {
    if (isListening) {
      isListening = false;
      mOutputDataListener = null;
      Intent mServiceIntent = new Intent(mContext, BLEService.class);
      mServiceIntent.setAction(BLEConstants.get(BLEConstants.TASK_STOP_DATA_STREAM));
      mContext.startService(mServiceIntent);
    }
  }
  
  public void startRecording(float recordingTime, final EkoStartRecordingListener ekoStartRecordingListener) {
    if (ekoStartRecordingListener == null) {
      throw new RuntimeException("EkoStartRecordingListener is not attached");
    }
    
    if (recordingTime <= 0.0F) {
      ekoStartRecordingListener.onInvalidInputTime();
      return;
    }
    
    if (isStreaming) {
      ekoStartRecordingListener.recordedAudioCurrentlyStreamingToDevice();
      return;
    }
    
    if (!isListening) {
      ekoStartRecordingListener.noStreamFromDevice();
      return;
    }
    
    isRecording = true;
    mRecordManager = new RecordStreamManager(mContext, getPeripheralType(), recordingTime, new RecordStreamManager.BaseRecordListener()
    {
      public void onRecordTimeEnd(File audioFile, File ecgFile) {
        isRecording = false;
        mRecordManager = null;
        ekoStartRecordingListener.onRecordTimeEnd(audioFile, ecgFile);
      }
      
      public void onError(Exception e)
      {
        isRecording = false;
        mRecordManager = null;
        ekoStartRecordingListener.onError(e);
      }
      
      public void onDataLosed(File recordFile)
      {
        isRecording = false;
        mRecordManager = null;
        ekoStartRecordingListener.onDataLosed(recordFile);
      }
    });
  }
  
  public void stopRecording(EkoStopRecordingListener stopRecordingListener) {
    if ((!isRecording) || (mRecordManager == null)) {
      if (stopRecordingListener != null) {
        stopRecordingListener.onNoRecordsFound();
      }
      return;
    }
    
    if (!mRecordManager.isRecordFileValid()) {
      if (stopRecordingListener != null) {
        stopRecordingListener.onWavFileInvalid();
      }
      return;
    }
    
    isRecording = false;
    if (stopRecordingListener != null) {
      stopRecordingListener.onSuccess(mRecordManager.onStop());
    }
    mRecordManager = null;
  }
  
  public void setAmplitudeScaleFactor(int amplitudeScaleFactor, EkoAmplitudeScaleFactorListener errorListener) {
    if (errorListener == null) {
      throw new RuntimeException("EkoAmplitudeScaleFactorListener can`t be null");
    }
    
    if (amplitudeScaleFactor <= 0) {
      errorListener.onInvalidScaleFactor();
      return;
    }
    
    Intent mServiceIntent = new Intent(mContext, BLEService.class);
    mServiceIntent.setAction(BLEConstants.get(BLEConstants.TASK_CHANGE_AMPLITUDE_SCALE_FACTOR));
    mServiceIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_AMPLITUDE_SCALE_FACTOR), amplitudeScaleFactor);
    mContext.startService(mServiceIntent);
  }
  
  public void getAmplitudeScaleFactor(EkoGetAmplitudeScaleFactor listener) {
    if (listener == null) {
      throw new RuntimeException("EkoGetAmplitudeScaleFactor is not attached");
    }
    
    mAmplitudeScaleFactorListener = listener;
    Intent streamingIntent = new Intent(mContext, BLEService.class);
    streamingIntent.setAction(BLEConstants.get(BLEConstants.TASK_GET_AMPLITUDE_SCALE_FACTOR));
    mContext.startService(streamingIntent);
  }
  
  private void initReceiver() {
    mReceiver = new BroadcastReceiver()
    {
      public void onReceive(Context context, Intent intent)
      {
        if (intent.getAction().equals(BLEConstants.get(BLEConstants.ACTION_DEVICE_DISCOVERED))) {
          Logger.e("BLEScanner_Service", "Device Discovered");
          
          String name = intent.getStringExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_NAME));
          String address = intent.getStringExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_ADDRESS));
          BLEDevice discoveredDevice = new BLEDevice(name, address);
          if (mScanListener != null) {
            mScanListener.foundDevice(discoveredDevice);
          }
        } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.ACTION_CONNECTION_STATUS_CHANGED))) {
          int status = intent.getIntExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_STATUS), 0);
          Log.d("LibCore", "Status Changed " + String.valueOf(status));
          if (status == 2) {
            Log.d("LibCore", "Connected to Device, Status Changed");
            String name = intent.getStringExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_NAME));
            String address = intent.getStringExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_ADDRESS));
            EKODevice ekoDevice = new EKODevice(name, address);
            mCurrentConnectedDevice = ekoDevice;
            if (mConnectListener != null) {
              mConnectListener.connectedToDevice(mCurrentConnectedDevice);
            }
            isConnecting = false;
          }
          else if (status == 0) {
            Log.d("LibCore", "Disconnected from Device, Status Changed");
            String name = intent.getStringExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_NAME));
            String address = intent.getStringExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_ADDRESS));
            EKODevice ekoDevice = new EKODevice(name, address);
            if (mCurrentConnectedDevice != null) {
              if (mConnectListener != null) {
                mConnectListener.deviceDidDisconnect(mCurrentConnectedDevice);
              }
            }
            else if (mConnectListener != null) {
              mConnectListener.failedToConnectToDevice("Pairing with device error. Please try again", ekoDevice);
            }
            
            isConnecting = false;
            mCurrentConnectedDevice = null;
          } else if (status == 1) {
            Log.d("LibCore", "Connecting to Device, Status Changed");
            isConnecting = true;
          }
        } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.ACTION_VOLUME_CHANGED))) {
          int volume = intent.getIntExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_VOLUME), 0);
          if (mCurrentConnectedDevice != null)
          {
            if (volume != mCurrentConnectedDevice.getVolumeLevel()) {
              mCurrentConnectedDevice.setVolumeLevel(volume);
              if (mVolumeListener != null) {
                mVolumeListener.deviceUpdatedVolumeLevel(volume);
              }
            }
          }
        } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.ACTION_BATTERY_CHANGED))) {
          int battery = intent.getIntExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_BATTERY), 0);
          if ((mCurrentConnectedDevice != null) && 
            (battery != mCurrentConnectedDevice.getBatteryLevel())) {
            mCurrentConnectedDevice.setBatteryLevel(battery);
            if (mBatteryListener != null) {
              mBatteryListener.deviceUpdatedBatteryLevel(battery);
            }
          }
        }
        else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_WRITE_STREAM))) {
          short[] audioStreamData = intent.getShortArrayExtra(BLEConstants.get(BLEConstants.EXTRAS_AUDIO_STREAM_DATA));
          byte[] audioStreamBytes = intent.getByteArrayExtra(BLEConstants.get(BLEConstants.EXTRAS_AUDIO_BYTE_DATA));
          short[] ecgStreamData = intent.getShortArrayExtra(BLEConstants.get(BLEConstants.EXTRAS_ECG_STREAM_DATA));
          byte[] ecgStreamBytes = intent.getByteArrayExtra(BLEConstants.get(BLEConstants.EXTRAS_ECG_BYTE_DATA));
          

          if ((isRecording) && (mRecordManager != null)) {
            mRecordManager.writeBytes(audioStreamBytes, ecgStreamBytes);
          }
          
          if (mOutputDataListener != null) {
            mOutputDataListener.deviceOutputtedAudio(audioStreamBytes);
            mOutputDataListener.deviceOutputtedAudioDataPoint(audioStreamData);
            if ((getPeripheralType() == LibCore.PeripheralType.EkoDuo) && (ecgStreamData != null)) {
              mOutputDataListener.deviceOutputtedECGDataPoint(ecgStreamData);
            }
          }
        } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.ACTION_ERROR))) {
          String error = intent.getStringExtra(BLEConstants.get(BLEConstants.EXTRAS_ERROR_MSG));
          if (mErrorListener != null) {
            mErrorListener.onError(error);
          }
        } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.ACTION_DATA_LOSS))) {
          if ((isRecording) && (mRecordManager != null)) {
            mRecordManager.onDataPacketLost();
          }
        } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.ACTION_GET_AMPLITUDE_SCALE_FACTOR))) {
          int amplitudeScaleFactor = intent.getIntExtra(BLEConstants.get(BLEConstants.EXTRAS_AMPLITUDE_SCALE_FACTOR), 0);
          if (mAmplitudeScaleFactorListener != null) {
            mAmplitudeScaleFactorListener.onAmplitudeReceived(amplitudeScaleFactor);
          }
          
        }
      }
    };
    IntentFilter filter = new IntentFilter(BLEConstants.get(BLEConstants.ACTION_DEVICE_DISCOVERED));
    filter.addAction(BLEConstants.get(BLEConstants.ACTION_CONNECTION_STATUS_CHANGED));
    filter.addAction(BLEConstants.get(BLEConstants.ACTION_VOLUME_CHANGED));
    filter.addAction(BLEConstants.get(BLEConstants.ACTION_BATTERY_CHANGED));
    filter.addAction(BLEConstants.get(BLEConstants.TASK_WRITE_STREAM));
    filter.addAction(BLEConstants.get(BLEConstants.ACTION_GET_AMPLITUDE_SCALE_FACTOR));
    LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
  }
  
  public void stop() {
    pause();
  }
  
  public void pause() {
    if (isScanning) {
      stopScanningForDevices();
    }
    if (isConnecting) {
      cancelDeviceConnection();
    }
    if (isStreaming) {
      stopStreamingAudioToConnectedDevice();
    }
    if (isListening) {
      stopStreamingFromDevice();
    }
    mOutputDataListener = null;
    mVolumeListener = null;
    mBatteryListener = null;
    mConnectListener = null;
    mScanListener = null;
    mAmplitudeScaleFactorListener = null;
    try {
      LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
    }
    catch (Exception localException) {}
  }
  
  public void resume()
  {
    try {
      LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
    }
    catch (Exception localException) {}
    
    IntentFilter filter = new IntentFilter(BLEConstants.get(BLEConstants.ACTION_DEVICE_DISCOVERED));
    filter.addAction(BLEConstants.get(BLEConstants.ACTION_CONNECTION_STATUS_CHANGED));
    filter.addAction(BLEConstants.get(BLEConstants.ACTION_VOLUME_CHANGED));
    filter.addAction(BLEConstants.get(BLEConstants.ACTION_BATTERY_CHANGED));
    filter.addAction(BLEConstants.get(BLEConstants.ACTION_GET_AMPLITUDE_SCALE_FACTOR));
    filter.addAction(BLEConstants.get(BLEConstants.TASK_WRITE_STREAM));
    LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
  }
}
