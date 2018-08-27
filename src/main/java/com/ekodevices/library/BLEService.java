//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ekodevices.library;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.bluetooth.le.ScanFilter.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Build.VERSION;
import android.support.annotation.WorkerThread;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.ekodevices.library.BluetoothReceiver.IBluetoothMessageListener;
import com.ekodevices.library.PacketSequenceObserver.IPacketSequenceListener;
import com.ekodevices.library.ServiceHandler.IHandlerListener;
import java.io.RandomAccessFile;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class BLEService extends Service implements IHandlerListener, IBluetoothMessageListener {
    static int lastVolume = 0;
    private static final String TAG = BLEService.class.getSimpleName();
    public int mConnectionState = 0;
    public boolean mIsConnecting = false;
    private int mConnectedPeripheral;
    private int counter = 0;
    private Timer mScanTimer;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private CountDownTimer mConnectionTimer;
    private BluetoothGatt mBluetoothGatt;
    private final IBinder mBinder = new BLEService.BLEBinder();
    private volatile Looper mServiceLooper;
    private volatile ServiceHandler mServiceHandler;
    public BluetoothGattCharacteristic mNotifyCharacteristic;
    @SuppressLint({"NewApi"})
    private ScanCallback mScanCallback;
    private LeScanCallback mLeScanCallback;
    private boolean mRedelivery;
    private boolean isScanning = false;
    private boolean isStreaming = false;
    private FilterCluster mFilterCluster;
    private ECGFilter mECGFilter;
    private PacketSequenceObserver mPacketSequenceObserver;
    private BroadcastReceiver mBluetoothReceiver;
    private BluetoothLeScanner mLeScanner;
    private LibEncoder mAudioDecoder;
    private LibEncoder mAudioEncoder;
    private LibEncoder mECGDecoder;
    private LibEncoder mECGEncoder;
    private boolean isRendering = false;
    private RandomAccessFile mWriteFileStream;
    private int mSequenceNumber;
    private int mStreamingIndex;
    private double mEcgGain;
    private double mExactDotsPerMillimeterX;
    private Long lastDisconnect = new Long(0L);
    private String mLastConnectedDeviceAddress = null;
    private Runnable mTaskCheckRunnable = null;
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        public void onConnectionStateChange(BluetoothGatt gatt, int status, final int newState) {
            Logger.e(this, "onConnectionStateChange " + String.valueOf(newState));
            if (BLEService.this.mConnectionState == 0 && newState == 2) {
                Long currentTime = System.currentTimeMillis();
                if (currentTime - BLEService.this.lastDisconnect < 1000L) {
                    Log.d(BLEService.TAG, "Skipping erroneous connect response");
                    return;
                }
            }

            if (newState == 2 && (BLEService.this.mBluetoothGatt == null || BLEService.this.mBluetoothGatt.getDevice() == null)) {
                Log.d(BLEService.TAG, "Skipping erroneous connect response, Gatt is null");
            } else {
                if (BLEService.this.mPacketSequenceObserver != null) {
                    BLEService.this.mPacketSequenceObserver.resetSavedPacketSequence();
                }

                int delay = 0;
                if (newState == 2 && BLEService.this.mBluetoothGatt != null) {
                    delay = 1500;
                }

                BLEService.this.mServiceHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (newState == 2 && BLEService.this.mBluetoothGatt != null) {
                            Log.d(BLEService.TAG, "Connected");
                            BLEService.this.getFilter().resetFilters();
                            BLEService.this.mConnectionState = 2;
                            Log.d(BLEService.TAG, "Set mLastConnectedDeviceAddress to " + BLEService.this.mBluetoothGatt.getDevice().getAddress());
                            BLEService.this.mLastConnectedDeviceAddress = BLEService.this.mBluetoothGatt.getDevice().getAddress();
                            if (VERSION.SDK_INT >= 21) {
                                BLEService.this.mBluetoothGatt.requestConnectionPriority(1);
                            }

                            BLEService.this.mBluetoothGatt.discoverServices();
                            Iterator var4 = BLEService.this.mBluetoothGatt.getServices().iterator();

                            while(var4.hasNext()) {
                                BluetoothGattService service = (BluetoothGattService)var4.next();
                                Log.d(BLEService.TAG, "Serivce UUID: " + service.getUuid());
                            }

                            if (BLEService.this.mScanTimer != null) {
                                BLEService.this.mScanTimer.cancel();
                                BLEService.this.mScanTimer = null;
                            }

                            String connectedDeviceName = BLEService.this.mBluetoothGatt.getDevice().getName();
                            if (connectedDeviceName.contains("Eko Core")) {
                                BLEService.this.mConnectedPeripheral = 4;
                            } else if (connectedDeviceName.contains("Eko Duo")) {
                                BLEService.this.mConnectedPeripheral = 5;
                            }

                            BLEService.this.mIsConnecting = false;
                        } else if (newState == 0 || BLEService.this.mBluetoothGatt == null) {
                            BLEService.this.mConnectionState = 0;
                            BLEService.this.lastDisconnect = System.currentTimeMillis();
                            if (BLEService.this.isStreaming) {
                                BLEService.this.stopStreaming();
                            }

                            if (BLEService.this.mBluetoothGatt != null) {
                                BluetoothDevice deviceToDisconnect = BLEService.this.mBluetoothGatt.getDevice();
                                Log.d(BLEService.TAG, "Unbonding");
                                BLEService.this.unbondDevice(deviceToDisconnect);
                                BLEService.this.mBluetoothGatt.close();
                                BLEService.this.mBluetoothGatt = null;
                            }

                            BLEService.this.isRendering = false;
                            if (BLEService.this.isStreaming) {
                                try {
                                    BLEService.this.mWriteFileStream.close();
                                } catch (Exception var3) {
                                    var3.printStackTrace();
                                }
                            }

                            Logger.e(BLEService.TAG, "Disconnect");
                            BLEService.this.mConnectedPeripheral = 0;
                        }

                        Log.d(BLEService.TAG, "Sending STATUS_CHANGED intent " + String.valueOf(newState));
                        Intent broadcastIntent = new Intent();
                        broadcastIntent.setAction(BLEConstants.get(BLEConstants.ACTION_CONNECTION_STATUS_CHANGED));
                        if (BLEService.this.mBluetoothGatt != null && BLEService.this.mBluetoothGatt.getDevice() != null) {
                            broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_ADDRESS), BLEService.this.mBluetoothGatt.getDevice().getAddress());
                            broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_NAME), BLEService.this.mBluetoothGatt.getDevice().getName());
                        }

                        broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_STATUS), newState);
                        LocalBroadcastManager.getInstance(BLEService.this.getApplicationContext()).sendBroadcast(broadcastIntent);
                    }
                }, (long)delay);
            }
        }

        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(BLEService.TAG, "onDescriptorWrite " + String.valueOf(descriptor.getValue()[0]) + descriptor.getValue()[1]);
            BLEService.this.mBluetoothGatt.readDescriptor(descriptor);
        }

        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(BLEService.TAG, "onDescriptorRead " + String.valueOf(descriptor.getValue()[0]) + descriptor.getValue()[1]);
        }

        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            Logger.e(this, "onServicesDiscovered");
            if (status == 0) {
                BLEService.this.mServiceHandler.postAtFrontOfQueue(new Runnable() {
                    public void run() {
                        if (gatt.getServices() == null) {
                            Log.d(BLEService.TAG, "GATT returned NULL services");
                        } else {
                            BluetoothGattService service = BLEService.this.mBluetoothGatt.getService(UUID.fromString(EKOAttributes.getEkoCoreData()));
                            if (service == null) {
                                Log.d(BLEService.TAG, "EKO Service is NULL in onServicesDiscovered");
                            } else {
                                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(EKOAttributes.getDataPacket()));
                                BLEService.this.mNotifyCharacteristic = characteristic;
                                BLEService.this.setCharacteristicNotification(characteristic, true);
                            }
                        }
                    }
                });
            }

        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (BLEService.this.mConnectionState != 3 && BLEService.this.mConnectionState != 0) {
                BLEService.this.mConnectionTimer.cancel();
                BLEService.this.mConnectionTimer.start();
                final byte[] packet = characteristic.getValue();
                int volumeLevel = -1;
                int batteryLevel = -1;
                int sequenceNumber = -1;
                if (BLEService.this.mConnectedPeripheral == 4) {
                    volumeLevel = 7 & packet[1];
                    batteryLevel = 127 & packet[0];
                    sequenceNumber = (120 & packet[1]) >> 3;
                } else if (BLEService.this.mConnectedPeripheral == 5) {
                    int batteryOrVolume = (128 & packet[1]) >> 7;
                    if (batteryOrVolume != 1) {
                        batteryLevel = 127 & packet[1];
                    } else {
                        volumeLevel = 127 & packet[1];
                    }

                    sequenceNumber = 15 & packet[0];
                }

                Intent broadcastIntentVolume;
                if (batteryLevel != -1) {
                    if (BLEService.this.mConnectedPeripheral == 4) {
                        batteryLevel = BLEUtil.adjustBatteryLevel(batteryLevel);
                    }

                    broadcastIntentVolume = new Intent();
                    broadcastIntentVolume.setAction(BLEConstants.get(BLEConstants.ACTION_BATTERY_CHANGED));
                    broadcastIntentVolume.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_BATTERY), batteryLevel);
                    LocalBroadcastManager.getInstance(BLEService.this.getApplicationContext()).sendBroadcast(broadcastIntentVolume);
                }

                if (volumeLevel != -1) {
                    broadcastIntentVolume = new Intent();
                    broadcastIntentVolume.setAction(BLEConstants.get(BLEConstants.ACTION_VOLUME_CHANGED));
                    if (volumeLevel != BLEService.lastVolume) {
                        Log.d(BLEService.TAG, "Read Volume " + String.valueOf(volumeLevel));
                        BLEService.lastVolume = volumeLevel;
                    }

                    broadcastIntentVolume.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_VOLUME), volumeLevel);
                    LocalBroadcastManager.getInstance(BLEService.this.getApplicationContext()).sendBroadcast(broadcastIntentVolume);
                }

                if (BLEService.this.mPacketSequenceObserver != null && BLEService.this.mConnectionState == 2) {
                    BLEService.this.mPacketSequenceObserver.checkPacketSequence(sequenceNumber, new IPacketSequenceListener() {
                        public void onSequenceCorrect() {
                            if (packet.length >= 20) {
                                if ((packet[0] & 128) != 0) {
                                    Log.d(BLEService.TAG, "Received Codec Reset");
                                    BLEService.this.isRendering = true;
                                    BLEService.this.mAudioDecoder.initialize();
                                    BLEService.this.mECGDecoder.initialize();
                                    BLEService.this.getFilter().resetFilters();
                                    Intent broadcastIntent = new Intent();
                                    broadcastIntent.setAction(BLEConstants.get(BLEConstants.ACTION_STREAM_RESET));
                                    LocalBroadcastManager.getInstance(BLEService.this.getApplicationContext()).sendBroadcast(broadcastIntent);
                                }

                                if (BLEService.this.isRendering) {
                                    BLEService.this.counter++;
                                    BLEService.this.mServiceHandler.post(new Runnable() {
                                        public void run() {
                                            BLEService.this.decodePacket2(packet);
                                        }
                                    });
                                }

                            } else {
                                if (BLEService.this.isStreaming) {
                                    for(int i = 0; i < 4; ++i) {
                                        BLEService.this.mServiceHandler.post(new Runnable() {
                                            public void run() {
                                                BLEService.this.sendAudioPacket(false);
                                            }
                                        });
                                    }
                                }

                                Log.d(BLEService.TAG, "Short Packet Received");
                            }
                        }

                        public void onSequenceInvalid() {
                            Log.d(BLEService.TAG, "Resetting Stream");
                            BLEService.this.resetStream();
                            BLEService.this.updateBroadcast(BLEConstants.get(BLEConstants.ACTION_DATA_LOSS));
                        }
                    });
                }

            } else {
                Log.d(BLEService.TAG, "onCharacteristicChanged but already disconnecting");
            }
        }
    };

    public BLEService() {
    }

    public BLEService(String name) {
    }

    public void onCreate() {
        super.onCreate();
        if (LibConstants.bleEnabled) {
            if (!this.initialize()) {
                this.stopSelf();
            } else {
                HandlerThread thread = new HandlerThread("BLEService[]");
                thread.start();
                this.mServiceLooper = thread.getLooper();
                this.mServiceHandler = new ServiceHandler(this.mServiceLooper, this);
                this.mAudioDecoder = new LibEncoder();
                this.mAudioEncoder = new LibEncoder();
                this.mECGDecoder = new LibEncoder();
                this.mECGEncoder = new LibEncoder();
                this.mPacketSequenceObserver = new PacketSequenceObserver();
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.bluetooth.device.action.ACL_CONNECTED");
                filter.addAction("android.bluetooth.device.action.ACL_DISCONNECT_REQUESTED");
                filter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED");
                this.mBluetoothReceiver = new BluetoothReceiver(this);
                this.registerReceiver(this.mBluetoothReceiver, filter);
                if (VERSION.SDK_INT >= 21) {
                    this.initScanCallback();
                } else {
                    this.initLeScanCallback();
                }

                WindowManager windowManager = (WindowManager)this.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics metrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getRealMetrics(metrics);
                double exactDotsPerMillimeterY = (double)metrics.ydpi / 25.4D;
                double amplitude = 10.0D * exactDotsPerMillimeterY;
                this.mEcgGain = amplitude / 150.0D;
            }
        }
    }

    private boolean initialize() {
        Intent broadcastIntent;
        if (!this.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            broadcastIntent = new Intent();
            broadcastIntent.setAction(BLEConstants.get(BLEConstants.ACTION_ERROR));
            broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_ERROR_MSG), "Bluetooth Disabled or Not Support BLE");
            LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(broadcastIntent);
            return false;
        } else {
            this.mBluetoothManager = (BluetoothManager)this.getSystemService(Context.BLUETOOTH_SERVICE);
            this.mBluetoothAdapter = this.mBluetoothManager.getAdapter();
            if (this.mBluetoothAdapter == null) {
                broadcastIntent = new Intent();
                broadcastIntent.setAction(BLEConstants.get(BLEConstants.ACTION_ERROR));
                broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_ERROR_MSG), "Bluetooth Disabled or Not Support BLE");
                LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(broadcastIntent);
                return false;
            } else {
                this.mConnectionTimer = new CountDownTimer(1000L, 1001L) {
                    public void onTick(long millisUntilFinished) {
                    }

                    public void onFinish() {
                        Log.d(BLEService.TAG, "Connection Timeout");
                        BLEService.this.disconnect();
                    }
                };
                if (this.mBluetoothAdapter != null && !this.mBluetoothAdapter.isEnabled()) {
                    broadcastIntent = new Intent();
                    broadcastIntent.setAction(BLEConstants.get(BLEConstants.ACTION_ERROR));
                    broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_ERROR_MSG), "Bluetooth Disabled");
                    LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(broadcastIntent);
                    return true;
                } else {
                    return true;
                }
            }
        }
    }

    public void onStart(Intent intent, int startId) {
        if (LibConstants.bleEnabled) {
            Message msg = this.mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            msg.obj = intent;
            this.mServiceHandler.sendMessage(msg);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        this.onStart(intent, startId);
        return this.mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public void onHandleMessage(Intent intent) {
        this.onHandleIntent(intent);
    }

    public boolean isEkoDevice(String deviceName) {
        Iterator var2 = BLEConstants.EKO_DEVICE_NAMES.iterator();

        String s;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            s = (String)var2.next();
        } while(!deviceName.contains(s));

        return true;
    }

    public void onBluetoothAction(BluetoothDevice device, String action) {
        if (!this.mIsConnecting) {
            boolean ekoDevice = this.isEkoDevice(device.getName());
            byte var5 = -1;
            switch(action.hashCode()) {
                case -301431627:
                    if (action.equals("android.bluetooth.device.action.ACL_CONNECTED")) {
                        var5 = 0;
                    }
                default:
                    switch(var5) {
                        case 0:
                            if (ekoDevice) {
                                this.unbondAllEkoDevicesExcludingEkoDevice(device);
                                if (this.mBluetoothGatt != null) {
                                    this.mBluetoothGatt.close();
                                }

                                this.mConnectionState = 0;
                                this.connect(device.getAddress());
                            }
                        default:
                    }
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(this.mBluetoothReceiver);
    }

    private FilterCluster getFilter() {
        if (this.mFilterCluster == null) {
            this.mFilterCluster = new FilterCluster(4000.0F, 30.0F, 800.0F);
        }

        return this.mFilterCluster;
    }

    private ECGFilter getECGFilter() {
        if (this.mECGFilter == null) {
            this.mECGFilter = new ECGFilter();
        }

        return this.mECGFilter;
    }

    public void scanLeDevice(boolean enable, int scanPeriod) {
        if (VERSION.SDK_INT >= 21) {
            this.mLeScanner = this.mBluetoothAdapter.getBluetoothLeScanner();
            if (this.mLeScanner == null) {
                return;
            }
        }

        if (enable) {
            if (this.mConnectionState == 1) {
                Logger.e(TAG, "stop scanning during connecting");
                return;
            }

            this.mServiceHandler.postDelayed(new Runnable() {
                public void run() {
                    BLEService.this.isScanning = false;
                    if (BLEService.this.mBluetoothAdapter.isEnabled()) {
                        if (VERSION.SDK_INT >= 21) {
                            BLEService.this.mLeScanner.flushPendingScanResults(BLEService.this.mScanCallback);
                            BLEService.this.mLeScanner.stopScan(BLEService.this.mScanCallback);
                        } else {
                            BLEService.this.mBluetoothAdapter.stopLeScan(BLEService.this.mLeScanCallback);
                        }
                    }

                }
            }, (long)scanPeriod);
            if (VERSION.SDK_INT >= 21) {
                UUID uuid = UUID.fromString(EKOAttributes.getEkoCoreData());
                ScanFilter mScanFilter = (new Builder()).setServiceUuid(new ParcelUuid(uuid)).build();
                ScanSettings mScanSettings = (new android.bluetooth.le.ScanSettings.Builder()).setScanMode(2).build();
                ArrayList<ScanFilter> mFilterList = new ArrayList();
                mFilterList.add(mScanFilter);
                this.isScanning = true;
                this.mLeScanner.startScan(mFilterList, mScanSettings, this.mScanCallback);
            } else {
                this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
            }
        } else if (VERSION.SDK_INT >= 21) {
            this.mLeScanner.flushPendingScanResults(this.mScanCallback);
            this.mLeScanner.stopScan(this.mScanCallback);
            this.isScanning = false;
        } else {
            this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
            this.isScanning = false;
        }

    }

    private void initLeScanCallback() {
        this.mLeScanCallback = new LeScanCallback() {
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                String name = "";
                if (TextUtils.isEmpty(device.getName())) {
                    name = BLEUtil.parseName(scanRecord);
                    if (TextUtils.isEmpty(name)) {
                        name = null;
                    }
                } else {
                    name = device.getName();
                }

                boolean isEko = false;
                String uid = EKOAttributes.getEkoCoreData();
                List<UUID> result = BLEUtil.parseUuids(scanRecord);

                for(int i = 0; i < result.size(); ++i) {
                    if (uid.equals(((UUID)result.get(i)).toString())) {
                        isEko = true;
                        break;
                    }
                }

                if (isEko) {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(BLEConstants.get(BLEConstants.ACTION_DEVICE_DISCOVERED));
                    broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_NAME), name);
                    broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_ADDRESS), device.getAddress());
                    LocalBroadcastManager.getInstance(BLEService.this.getApplicationContext()).sendBroadcast(broadcastIntent);
                }

            }
        };
    }

    public void setIntentRedelivery(boolean enabled) {
        this.mRedelivery = enabled;
    }

    @TargetApi(21)
    @SuppressLint({"NewApi"})
    private void initScanCallback() {
        this.mScanCallback = new ScanCallback() {
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                String name = "";
                if (TextUtils.isEmpty(result.getDevice().getName())) {
                    name = BLEUtil.parseName(result.getScanRecord().getBytes());
                    if (TextUtils.isEmpty(name)) {
                        name = null;
                    }
                } else {
                    name = result.getDevice().getName();
                }

                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(BLEConstants.get(BLEConstants.ACTION_DEVICE_DISCOVERED));
                broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_NAME), name);
                broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_ADDRESS), result.getDevice().getAddress());
                LocalBroadcastManager.getInstance(BLEService.this.getApplicationContext()).sendBroadcast(broadcastIntent);
            }
        };
    }

    public boolean connect(String address) {
        Log.d(TAG, "Entered Connect");
        if (this.mBluetoothAdapter != null && address != null) {
            this.mConnectionTimer.cancel();
            if (this.mBluetoothGatt == null) {
                Logger.e(TAG, "BT Gatt Null");
                this.mConnectionState = 0;
            }

            if (this.mConnectionState != 1 && this.mConnectionState != 2) {
                this.mBluetoothAdapter.cancelDiscovery();
                final BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(address);
                this.unbondAllEkoDevicesExcludingEkoDevice(device);
                if (this.mBluetoothGatt != null) {
                    Logger.i(TAG, "stale gatt");
                    this.disconnect();
                    this.mBluetoothGatt.close();
                    this.mBluetoothGatt = null;
                } else {
                    Logger.i(TAG, "fresh gatt");
                }

                this.mIsConnecting = true;
                this.mServiceHandler.postDelayed(new Runnable() {
                    public void run() {
                        Logger.e(BLEService.TAG, "Start Connecting");
                        BLEService.this.mBluetoothGatt = device.connectGatt(BLEService.this, false, BLEService.this.mGattCallback);
                        if (BLEService.this.mBluetoothGatt == null) {
                            Log.d(BLEService.TAG, "Connect returned NULL gatt");
                        }

                    }
                }, 500L);
                return true;
            } else {
                Logger.e(TAG, "Already Connected");
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(BLEConstants.get(BLEConstants.ACTION_CONNECTION_STATUS_CHANGED));
                if (this.mBluetoothGatt != null && this.mBluetoothGatt.getDevice() != null) {
                    broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_ADDRESS), this.mBluetoothGatt.getDevice().getAddress());
                    broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_NAME), this.mBluetoothGatt.getDevice().getName());
                }

                broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_STATUS), 2);
                LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(broadcastIntent);
                return true;
            }
        } else {
            Logger.e(TAG, "BT Adapter Null");
            return false;
        }
    }

    private void updateBroadcast(String action) {
        Intent broadcastIntentBattery = new Intent();
        broadcastIntentBattery.setAction(action);
        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(broadcastIntentBattery);
    }

    public void resetStream() {
        Log.d(TAG, "Entered resetStream");
        if (this.mBluetoothGatt == null) {
            Log.d(TAG, "Gatt is null, skipping stream reset");
        } else {
            Log.d(TAG, "Gatt is not null, resetting stream");
            if (!this.isRendering) {
                this.mAudioDecoder.initialize();
                this.mECGDecoder.initialize();
            }

            BluetoothGattService service = this.mBluetoothGatt.getService(UUID.fromString(EKOAttributes.getEkoCoreData()));
            if (service == null) {
                Log.d(TAG, "Service is null in resetStream, but Gatt is not null");
                this.disconnect();
            } else {
                BluetoothGattCharacteristic charac = service.getCharacteristic(UUID.fromString(EKOAttributes.getDataPacket()));
                if (this.mConnectedPeripheral == 5) {
                    charac.setWriteType(1);
                }

                byte[] resetPacket = new byte[]{-128, 0, 0, 0};
                charac.setValue(resetPacket);
                Logger.e("Reset Stream", "OK");
                if (!this.mBluetoothGatt.writeCharacteristic(charac)) {
                    Logger.e("writeCharacteristic", "ERR");
                    this.mServiceHandler.postDelayed(new Runnable() {
                        public void run() {
                            Log.d(BLEService.TAG, "Retry reset stream");
                            BLEService.this.resetStream();
                        }
                    }, 500L);
                } else {
                    Logger.e("writeCharacteristic", "OK");
                }

            }
        }
    }

    public void decodePacket2(byte[] packet) {
        byte[] audioPacket = new byte[16];

        for(int i = 0; i < 16; ++i) {
            audioPacket[i] = packet[i + 4];
        }

        byte[] decodedBytes = this.mAudioDecoder.decode(audioPacket);
        short[] streamData = new short[decodedBytes.length / 2];
        byte[] streamBytes = new byte[decodedBytes.length];


        for(int m = 0; m < decodedBytes.length / 2; ++m) {
            short val = (short)((decodedBytes[2 * m + 1] & 255) << 8 | decodedBytes[2 * m] & 255);
            int tempVal = ((int) val) * 8;

            if (tempVal > Short.MAX_VALUE) {
                tempVal = Short.MAX_VALUE;
            } else if (tempVal < Short.MIN_VALUE) {
                tempVal = Short.MIN_VALUE;
            }

           // i = this.getFilter().filterSample(i);
            short partOfStream = (short)tempVal;
            streamData[m] = partOfStream;
            streamBytes[2 * m + 1] = (byte)(partOfStream >> 8 & 255);
            streamBytes[2 * m] = (byte)(partOfStream & 255);
        }

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(BLEConstants.get(BLEConstants.TASK_WRITE_STREAM));
        broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_AUDIO_STREAM_DATA), streamData);
        broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_AUDIO_BYTE_DATA), streamBytes);
        if (this.mConnectedPeripheral == 5) {
            byte[] ecgPacket = new byte[2];

            for(int i = 0; i < 2; ++i) {
                ecgPacket[i] = packet[i + 2];
            }

            byte[] ecgDecodedBytes = this.mECGDecoder.decode(ecgPacket);
            short[] ecgStreamData = new short[ecgDecodedBytes.length / 2];
            byte[] ecgStreamBytes = new byte[ecgDecodedBytes.length];

            for(int i = 0; i < ecgDecodedBytes.length / 2; ++i) {
                short val = (short)((ecgDecodedBytes[2 * i + 1] & 255) << 8 | ecgDecodedBytes[2 * i] & 255);
                ecgStreamData[i] = (short)((int)(this.mEcgGain * (double)this.getECGFilter().filterSample(val)));
                ecgStreamBytes[2 * i + 1] = (byte)(val >> 8 & 255);
                ecgStreamBytes[2 * i] = (byte)(val & 255);
            }

            broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_ECG_STREAM_DATA), ecgStreamData);
            broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_ECG_BYTE_DATA), ecgStreamBytes);
        }

        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(broadcastIntent);
        --this.counter;
    }

    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        UUID UUID_DATA_PACKET = UUID.fromString(EKOAttributes.getDataPacket());
        if (this.mBluetoothAdapter != null && this.mBluetoothGatt != null) {
            this.mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
            if (UUID_DATA_PACKET.equals(characteristic.getUuid())) {
                if (this.mConnectedPeripheral == 5) {
                    characteristic.setWriteType(2);
                }

                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(EKOAttributes.getClientCharacteristic()));
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                this.mBluetoothGatt.writeDescriptor(descriptor);
            }

            this.mConnectionTimer.start();
        }
    }

    public void close() {
        if (this.mBluetoothGatt != null) {
            Log.d(TAG, "Gatt closed");
            this.mBluetoothGatt.close();
            this.mBluetoothGatt = null;
            this.mConnectionState = 0;
        }
    }

    public void disconnect() {
        Log.d(TAG, "Disconnecting");
        Log.d(TAG, "Cancelling Timer");
        this.mConnectionTimer.cancel();
        if (this.mBluetoothAdapter == null) {
            Log.d(TAG, "Bluetooth Adapter is null");
        } else if (this.mBluetoothGatt == null) {
            Log.d(TAG, "Bluetooth GATT is null");
        } else if (this.mConnectionState != 0 && this.mConnectionState != 3) {
            try {
                this.mConnectionState = 3;
                this.mBluetoothGatt.disconnect();
            } catch (Exception var2) {
                var2.printStackTrace();
            }

            Log.d(TAG, "Disconnected");
        } else {
            Log.d(TAG, "Already disconnected or disconnecting");
        }
    }

    private void closeConnection() {
        if (this.mConnectionState == 0) {
            this.flush();
            this.mServiceHandler.post(new Runnable() {
                public void run() {
                    BLEService.this.close();
                }
            });
        } else if (this.mConnectionState == 2) {
            this.mServiceHandler.post(new Runnable() {
                public void run() {
                    BLEService.this.disconnect();
                }
            });
        } else if (this.mConnectionState == 1) {
            this.mServiceHandler.postAtFrontOfQueue(new Runnable() {
                public void run() {
                    BLEService.this.cancelConnect();
                }
            });
        }

    }

    public void cancelConnect() {
        if (this.mConnectionState == 1) {
            if (this.mBluetoothAdapter == null || this.mBluetoothGatt == null) {
                return;
            }

            this.mBluetoothGatt.disconnect();
            this.mConnectionState = 3;
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(BLEConstants.get(BLEConstants.ACTION_CONNECTION_STATUS_CHANGED));
            broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_STATUS), 3);
            LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(broadcastIntent);
            this.close();
        }

    }

    public void setVolume(int volume) {
        int maxVolume = 0;
        switch(this.mConnectedPeripheral) {
            case 4:
                maxVolume = LibCore.VolumeMaxCore;
                break;
            case 5:
                maxVolume = LibCore.VolumeMaxDuo;
        }

        if (volume > maxVolume) {
            volume = maxVolume;
        } else if (volume < 0) {
            volume = 0;
        }

        byte[] volumePacket = new byte[]{0, (byte)(128 + volume & 255), 0, 0};
        BluetoothGattService service = this.mBluetoothGatt.getService(UUID.fromString(EKOAttributes.getEkoCoreData()));
        BluetoothGattCharacteristic charac = service.getCharacteristic(UUID.fromString(EKOAttributes.getDataPacket()));
        if (this.mConnectedPeripheral == 5) {
            charac.setWriteType(1);
        }

        charac.setValue(volumePacket);
        this.mBluetoothGatt.writeCharacteristic(charac);
        Log.d(TAG, "Set Volume " + String.valueOf(volume));
    }

    public void sendAudio(final byte[] packet) {
        BluetoothGattService service = this.mBluetoothGatt.getService(UUID.fromString(EKOAttributes.getEkoCoreData()));
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(EKOAttributes.getDataPacket()));
        characteristic.setValue(packet);
        if (!this.mBluetoothGatt.writeCharacteristic(characteristic)) {
            this.mServiceHandler.postAtFrontOfQueue(new Runnable() {
                public void run() {
                    BLEService.this.sendAudio(packet);
                }
            });
        }

    }

    @WorkerThread
    protected void onHandleIntent(final Intent intent) {
        Intent broadcastIntentBattery;
        if (!LibConstants.bleEnabled) {
            broadcastIntentBattery = new Intent();
            broadcastIntentBattery.setAction(BLEConstants.get(BLEConstants.ACTION_ERROR));
            broadcastIntentBattery.putExtra(BLEConstants.get(BLEConstants.EXTRAS_ERROR_MSG), "Bluetooth Disabled or Not Support BLE");
            LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(broadcastIntentBattery);
        } else if (intent != null && intent.getAction() != null) {
            if (this.mBluetoothAdapter != null && !this.mBluetoothAdapter.isEnabled()) {
                if (!intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_RECONNECT))) {
                    broadcastIntentBattery = new Intent();
                    broadcastIntentBattery.setAction(BLEConstants.get(BLEConstants.ACTION_ERROR));
                    broadcastIntentBattery.putExtra(BLEConstants.get(BLEConstants.EXTRAS_ERROR_MSG), "Bluetooth Disabled");
                    LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(broadcastIntentBattery);
                }

            } else {
                if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_SCAN))) {
                    if (this.mScanTimer != null) {
                        this.mScanTimer.cancel();
                        this.mScanTimer = null;
                    }

                    this.mScanTimer = new Timer();
                    this.mScanTimer.scheduleAtFixedRate(new BLEService.ScanTimerTask(), 1000L, 8000L);
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_STOP_SCAN))) {
                    if (this.mScanTimer != null) {
                        this.mScanTimer.cancel();
                        this.mScanTimer = null;
                    }

                    try {
                        if (VERSION.SDK_INT >= 21) {
                            this.mLeScanner.stopScan(this.mScanCallback);
                        } else {
                            this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
                        }
                    } catch (Exception var4) {
                        ;
                    }
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_CONNECT))) {
                    this.mServiceHandler.removeCallbacks(this.mTaskCheckRunnable);
                    final String address = intent.getStringExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_ADDRESS));
                    int delay = 0;
                    if (address.equals(this.mLastConnectedDeviceAddress)) {
                        Log.d(TAG, "Using delay on connect " + this.mLastConnectedDeviceAddress + " " + address);
                        delay = 7500;
                    } else {
                        Log.d(TAG, "Not delay on connect" + this.mLastConnectedDeviceAddress + " " + address);
                    }

                    this.mServiceHandler.postDelayed(new Runnable() {
                        public void run() {
                            Log.d(BLEService.TAG, "Connecting to " + address);
                            BLEService.this.connect(address);
                        }
                    }, (long)delay);
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_CHECK))) {
                    this.mTaskCheckRunnable = new Runnable() {
                        public void run() {
                            Log.d(BLEService.TAG, "TASK_CHECK");
                            if (BLEService.this.mConnectionState == 2 && BLEService.this.mBluetoothGatt != null && BLEService.this.mBluetoothGatt.getDevice() != null && BLEService.this.mBluetoothGatt.getDevice().getAddress() != null) {
                                Intent broadcastIntent = new Intent();
                                broadcastIntent.setAction(BLEConstants.get(BLEConstants.ACTION_CONNECTION_STATUS_CHANGED));
                                broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_ADDRESS), BLEService.this.mBluetoothGatt.getDevice().getAddress());
                                broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_NAME), BLEService.this.mBluetoothGatt.getDevice().getName());
                                broadcastIntent.putExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_STATUS), 2);
                                LocalBroadcastManager.getInstance(BLEService.this.getApplicationContext()).sendBroadcast(broadcastIntent);
                            } else {
                                BLEService.this.connectToBondedDevice();
                            }

                        }
                    };
                    this.mServiceHandler.postDelayed(this.mTaskCheckRunnable, 1500L);
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_CHANGE_VOLUME))) {
                    this.mServiceHandler.postAtFrontOfQueue(new Runnable() {
                        public void run() {
                            BLEService.this.setVolume(intent.getIntExtra(BLEConstants.get(BLEConstants.EXTRAS_DEVICE_VOLUME), 0));
                        }
                    });
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_START_STREAMING))) {
                    this.mStreamingIndex = intent.getIntExtra(BLEConstants.get(BLEConstants.EXTRAS_STREAMING_INDEX), 0);
                    this.startStreaming();
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_STOP_STREAMING))) {
                    if (this.isStreaming) {
                        this.stopStreaming();
                    }
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_RESET_DATA_STREAM))) {
                    this.mServiceHandler.post(new Runnable() {
                        public void run() {
                            Log.d(BLEService.TAG, "TASK_RESET_DATA_STREAM requests resetStream()");
                            BLEService.this.resetStream();
                        }
                    });
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_STOP_DATA_STREAM))) {
                    this.isRendering = false;
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_CLEAR_TASKS))) {
                    this.clearTaskQueue();
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_CLOSE))) {
                    this.closeConnection();
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_DISCONNECT))) {
                    Log.d(TAG, "Set mLastConnectedDeviceAddress to null");
                    this.mLastConnectedDeviceAddress = null;
                    this.disconnect();
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_FILTER))) {
                    this.getFilter().resetFilters();
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_CHANGE_AMPLITUDE_SCALE_FACTOR))) {
                    this.getFilter().setAmplitudeScaleFactor(intent.getIntExtra(BLEConstants.get(BLEConstants.EXTRAS_AMPLITUDE_SCALE_FACTOR), 1));
                } else if (intent.getAction().equals(BLEConstants.get(BLEConstants.TASK_GET_AMPLITUDE_SCALE_FACTOR))) {
                    broadcastIntentBattery = new Intent();
                    broadcastIntentBattery.setAction(BLEConstants.get(BLEConstants.ACTION_GET_AMPLITUDE_SCALE_FACTOR));
                    broadcastIntentBattery.putExtra(BLEConstants.get(BLEConstants.EXTRAS_AMPLITUDE_SCALE_FACTOR), this.getFilter().getAmplitudeScaleFactor());
                    LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(broadcastIntentBattery);
                }

            }
        }
    }

    public void flush() {
        this.mServiceHandler.removeCallbacksAndMessages((Object)null);
    }

    private void clearTaskQueue() {
        this.mServiceHandler.removeCallbacks((Runnable)null);
    }

    private void startStreaming() {
        if (!this.isStreaming) {
            try {
                this.mSequenceNumber = 0;
                this.isStreaming = true;
                this.mAudioEncoder.initialize();
                int i;
                if (LibFileStreamer.getInstance().getMode() == 1) {
                    this.mWriteFileStream = new RandomAccessFile(LibFileStreamer.getInstance().getFileCached(), "r");
                    this.mWriteFileStream.seek((long)(this.mStreamingIndex * 2 + 44));
                    this.mServiceHandler.post(new Runnable() {
                        public void run() {
                            BLEService.this.sendAudioPacket(true);
                        }
                    });

                    for(i = 0; i < 3; ++i) {
                        this.mServiceHandler.post(new Runnable() {
                            public void run() {
                                BLEService.this.sendAudioPacket(false);
                            }
                        });
                    }
                } else if (LibFileStreamer.getInstance().getMode() == 2) {
                    this.mServiceHandler.post(new Runnable() {
                        public void run() {
                            BLEService.this.sendAudioPacketByte(true);
                        }
                    });

                    for(i = 0; i < 3; ++i) {
                        this.mServiceHandler.post(new Runnable() {
                            public void run() {
                                BLEService.this.sendAudioPacketByte(false);
                            }
                        });
                    }
                }

                Logger.e("Streaming_END", "test1");
            } catch (Exception var2) {
                var2.printStackTrace();
            }

            Logger.e("Streaming_END", "test2");
        }

    }

    private void sendAudioPacketByte(boolean resetDecoder) {
        byte[] audioPacket = LibFileStreamer.getInstance().getStreamContent();
        if (audioPacket.length == 16) {
            final byte[] finalPacket = new byte[20];
            if (resetDecoder) {
                finalPacket[0] = -128;
            } else {
                finalPacket[0] = 0;
            }

            finalPacket[1] = (byte)(this.mSequenceNumber << 3 & 120);
            finalPacket[2] = 0;
            finalPacket[3] = 0;
            System.arraycopy(audioPacket, 0, finalPacket, 4, audioPacket.length);
            BluetoothGattService service = this.mBluetoothGatt.getService(UUID.fromString(EKOAttributes.getEkoCoreData()));
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(EKOAttributes.getDataPacket()));
            characteristic.setValue(finalPacket);
            if (!this.mBluetoothGatt.writeCharacteristic(characteristic)) {
                this.mServiceHandler.postAtFrontOfQueue(new Runnable() {
                    public void run() {
                        BLEService.this.sendAudio(finalPacket);
                    }
                });
            }

            this.mSequenceNumber = (this.mSequenceNumber + 1) % 16;
        }
    }

    private void sendAudioPacket(boolean resetDecoder) {
        if (resetDecoder) {
            try {
                this.mWriteFileStream.seek((long)(this.mStreamingIndex * 2 + 44));
            } catch (Exception var7) {
                var7.printStackTrace();
            }
        }

        int[] packetContent = this.getStreamPacket();
        if (packetContent != null) {
            byte[] audioPacket = this.mAudioEncoder.encode(packetContent);
            if (audioPacket.length == 16) {
                final byte[] finalPacket = new byte[20];
                if (resetDecoder) {
                    finalPacket[0] = -128;
                } else {
                    finalPacket[0] = 0;
                }

                finalPacket[1] = (byte)(this.mSequenceNumber << 3 & 120);
                finalPacket[2] = 0;
                finalPacket[3] = 0;
                System.arraycopy(audioPacket, 0, finalPacket, 4, audioPacket.length);
                BluetoothGattService service = this.mBluetoothGatt.getService(UUID.fromString(EKOAttributes.getEkoCoreData()));
                BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(EKOAttributes.getDataPacket()));
                characteristic.setValue(finalPacket);
                if (!this.mBluetoothGatt.writeCharacteristic(characteristic)) {
                    this.mServiceHandler.postAtFrontOfQueue(new Runnable() {
                        public void run() {
                            BLEService.this.sendAudio(finalPacket);
                        }
                    });
                }

                this.mSequenceNumber = (this.mSequenceNumber + 1) % 16;
            }
        }
    }

    public int[] getStreamPacket() {
        try {
            if (this.mWriteFileStream.length() - this.mWriteFileStream.getFilePointer() <= 64L) {
                this.stopStreaming();
                return null;
            }
        } catch (Exception var5) {
            this.stopStreaming();
            return null;
        }

        int[] ret = new int[32];

        for(int i = 0; i < 32; ++i) {
            try {
                byte[] b = new byte[2];
                this.mWriteFileStream.read(b);
                ret[i] = (short)((b[1] & 255) << 8 | b[0] & 255);
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }

        return ret;
    }

    private void stopStreaming() {
        if (this.isStreaming) {
            try {
                this.isStreaming = false;
                this.mWriteFileStream.close();
            } catch (Exception var2) {
                var2.printStackTrace();
            }

            if (LibCore.getInstance(this).isStreamingToDevice()) {
                LibCore.getInstance(this).stopStreamingAudioToConnectedDevice();
            }
        }

    }

    private void connectToBondedDevice() {
        if (this.mConnectionState != 2) {
            int numberOfBondedEkoDevices = this.getNumberOfBondedEkoDevices();
            if (this.mBluetoothAdapter != null && this.mBluetoothAdapter.isEnabled() && numberOfBondedEkoDevices > 0) {
                Iterator var2 = this.mBluetoothAdapter.getBondedDevices().iterator();

                while(var2.hasNext()) {
                    BluetoothDevice theDevice = (BluetoothDevice)var2.next();
                    if (this.isEkoDevice(theDevice.getName())) {
                        this.connect(theDevice.getAddress());
                        return;
                    }
                }
            }

        }
    }

    private int getNumberOfBondedEkoDevices() {
        int numberBondedCores = 0;
        if (this.mBluetoothAdapter.isEnabled()) {
            Iterator var2 = this.mBluetoothAdapter.getBondedDevices().iterator();

            while(var2.hasNext()) {
                BluetoothDevice theDevice = (BluetoothDevice)var2.next();
                if (this.isEkoDevice(theDevice.getName())) {
                    ++numberBondedCores;
                }
            }
        }

        return numberBondedCores;
    }

    private void unbondAllEkoDevicesExcludingEkoDevice(BluetoothDevice device) {
        if (device.getName() != null && this.isEkoDevice(device.getName()) && this.mBluetoothAdapter != null && this.mBluetoothAdapter.isEnabled()) {
            Iterator var2 = this.mBluetoothAdapter.getBondedDevices().iterator();

            while(true) {
                BluetoothDevice theDevice;
                do {
                    do {
                        if (!var2.hasNext()) {
                            return;
                        }

                        theDevice = (BluetoothDevice)var2.next();
                    } while(theDevice == null);
                } while(device.getAddress() != null && theDevice.getAddress() != null && device.getAddress().equals(theDevice.getAddress()));

                if (theDevice.getName() != null && this.isEkoDevice(theDevice.getName())) {
                    this.unbondDevice(theDevice);
                }
            }
        }
    }

    private void unbondDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[])null);
            m.invoke(device, (Object[])null);
        } catch (Exception var3) {
            Log.e(TAG, var3.getMessage());
        }

    }

    private class ScanTimerTask extends TimerTask {
        private ScanTimerTask() {
        }

        public void run() {
            if (BLEService.this.mBluetoothAdapter == null || BLEService.this.mBluetoothAdapter.isEnabled()) {
                BLEService.this.mServiceHandler.post(new Runnable() {
                    public void run() {
                        if (BLEService.this.mConnectionState != 1 && BLEService.this.mConnectionState != 2 && !BLEService.this.isScanning) {
                            BLEService.this.scanLeDevice(true, 8000);
                        }

                    }
                });
            }
        }
    }

    class BLEBinder extends Binder {
        BLEBinder() {
        }

        public BLEService getService() {
            return BLEService.this;
        }
    }
}
