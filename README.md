# Android-Bluetooth-Library
A library that will allow the user to interact with Eko hardware

# Library Instalation Instructions

**1. Put `eko_lib_{last_library_version}.jar` file into `app/libs` folder of your project.**

**2. Add permissions in Your `AndroidManifest.xml` file:**

```	
    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-feature
        android:name="android.hardware.bluetooth_le"
        android:required="true" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```
	
**3. Declare `BLEService` service in Your `AndroidManifest.xml` file**

```
	<service android:name="com.ekodevices.library.BLEService" />
```

**4. Use `LibCore` class methods for work with Device**

	
# Tips:
**1. To prevent Permission Exceptions Your need to request permission manually (actual for Android 6+)**

**For example:**

**Place this code in your `onCreate` method of `MainActivity`**
```
		LibCore.getInstance(getApplicationContext()).checkPermission(this);
```

**Add `onRequestPermissionResult` to handle result**
```
		 @Override
		public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
			LibCore.getInstance(getApplicationContext()).onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
```		
**2. Before start scanning for devices check if lib ready for scanning.**
**For this use function:**
```
	LibCore.getInstance(getApplicationContext()).isReady();
```

**3. When you call `LibCore` methods use `ApplicationContext`**

	
