package com.headphonedetectv2;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.module.annotations.ReactModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Map;
import java.util.HashMap;
import javax.annotation.Nullable;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import androidx.annotation.NonNull;

@ReactModule(name = HeadphoneDetectV2Module.NAME)
public class HeadphoneDetectV2Module extends NativeHeadphoneDetectSpec {
  public static final String NAME = "HeadphoneDetectV2";
  private static final String TAG = "HeadphoneDetectV2"; // Tag for logging
  private static final String AUDIO_DEVICE_CHANGED_NOTIFICATION = "AUDIO_DEVICE_CHANGED_NOTIFICATION";
  private BroadcastReceiver receiver;

  public HeadphoneDetectV2Module(ReactApplicationContext reactContext) {
    super(reactContext);
    Log.d(TAG, "Module initialized");
  }

  private void maybeRegisterReceiver() {
    final ReactApplicationContext reactContext = getReactApplicationContext();

    if (receiver != null) {
      Log.d(TAG, "Receiver already registered, skipping registration");
      return;
    }

    Log.d(TAG, "Creating new broadcast receiver");
    receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Received action: " + action);

        if (action != null) {
          switch (action) {
            case BluetoothDevice.ACTION_ACL_CONNECTED:
              Log.d(TAG, "Bluetooth ACL Connected");
              break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
              Log.d(TAG, "Bluetooth ACL Disconnected");
              break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
              int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
              Log.d(TAG, "Bluetooth Adapter State Changed: " + getBluetoothStateString(state));
              break;
            case BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED:
              Log.d(TAG, "Bluetooth Connection State Changed");
              break;
            case AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED:
              Log.d(TAG, "SCO Audio State Updated");
              break;
            case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
              Log.d(TAG, "Audio Becoming Noisy");
              break;
            case Intent.ACTION_HEADSET_PLUG:
              int headsetState = intent.getIntExtra("state", -1);
              Log.d(TAG, "Headset Plug State Changed: " + headsetState);
              break;
            default:
              Log.d(TAG, "Unhandled action: " + action);
              break;
          }

          // Get updated device state
          WritableMap updatedState = isAudioDeviceConnected();
          Log.d(TAG, "Updated device state - Bluetooth: " + updatedState.getBoolean("bluetooth") +
            ", AudioJack: " + updatedState.getBoolean("audioJack"));
          sendEvent(reactContext, AUDIO_DEVICE_CHANGED_NOTIFICATION, updatedState);
        }
      }
    };

    Log.d(TAG, "Registering intent filters");
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_HEADSET_PLUG);
    filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
    filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
    filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
    filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
    filter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
    filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Log.d(TAG, "Registering receiver with RECEIVER_NOT_EXPORTED flag");
        reactContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED);
      } else {
        Log.d(TAG, "Registering receiver without flags");
        reactContext.registerReceiver(receiver, filter);
      }
    } catch (Exception e) {
      Log.e(TAG, "Error registering receiver: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private void maybeUnregisterReceiver() {
    if (receiver == null) {
      Log.d(TAG, "No receiver to unregister");
      return;
    }

    try {
      Log.d(TAG, "Unregistering receiver");
      getReactApplicationContext().unregisterReceiver(receiver);
      receiver = null;
    } catch (Exception e) {
      Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
      e.printStackTrace();
    }
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put(AUDIO_DEVICE_CHANGED_NOTIFICATION, AUDIO_DEVICE_CHANGED_NOTIFICATION);
    return constants;
  }

  private WritableMap isAudioDeviceConnected() {
    final Map<String, Boolean> res = new HashMap<>();
    AudioManager audioManager = (AudioManager) getReactApplicationContext().getSystemService(Context.AUDIO_SERVICE);

    res.put("audioJack", false);
    res.put("bluetooth", false);

    Log.d(TAG, "Checking audio device connections");

    // Check Bluetooth adapter state
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    boolean bluetoothEnabled = bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    Log.d(TAG, "Bluetooth Adapter enabled: " + bluetoothEnabled);

    if (bluetoothEnabled) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
        Log.d(TAG, "Found " + devices.length + " audio devices");

        for (AudioDeviceInfo device : devices) {
          int type = device.getType();
          Log.d(TAG, "Checking device type: " + getAudioDeviceTypeString(type));

          if (type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
            type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
            type == AudioDeviceInfo.TYPE_USB_HEADSET) {
            res.put("audioJack", true);
            Log.d(TAG, "Wired audio device detected");
          }

          if (type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
            type == AudioDeviceInfo.TYPE_BLE_HEADSET) {
            res.put("bluetooth", true);
            Log.d(TAG, "Bluetooth audio device detected");
          }
        }
      } else {
        // Fallback for older Android versions
        boolean isBluetoothConnected = audioManager.isBluetoothA2dpOn() || audioManager.isBluetoothScoOn();
        res.put("bluetooth", isBluetoothConnected);
        Log.d(TAG, "Legacy Bluetooth check result: " + isBluetoothConnected);
      }
    }

    WritableMap map = new WritableNativeMap();
    for (Map.Entry<String, Boolean> entry : res.entrySet()) {
      map.putBoolean(entry.getKey(), entry.getValue());
    }

    Log.d(TAG, "Final device state - Bluetooth: " + map.getBoolean("bluetooth") +
      ", AudioJack: " + map.getBoolean("audioJack"));
    return map;
  }

  private void sendEvent(ReactApplicationContext reactContext, String eventName, @Nullable WritableMap params) {
    Log.d(TAG, "Sending event: " + eventName + " with params: " + params);
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  @Override
  public void isAudioDeviceConnected(final Promise promise) {
    Log.d(TAG, "isAudioDeviceConnected method called");
    promise.resolve(isAudioDeviceConnected());
  }

  @NonNull
  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public void initialize() {
    Log.d(TAG, "Initializing module");
    maybeRegisterReceiver();
  }

  @Override
  public void addListener(String eventName) {
    // Required for RN built in Event Emitter Calls
  }

  @Override
  public void removeListeners(double count) {
    // Required for RN built in Event Emitter Calls
  }

  // Helper method to convert Bluetooth states to readable strings
  private String getBluetoothStateString(int state) {
    switch (state) {
      case BluetoothAdapter.STATE_OFF: return "STATE_OFF";
      case BluetoothAdapter.STATE_TURNING_ON: return "STATE_TURNING_ON";
      case BluetoothAdapter.STATE_ON: return "STATE_ON";
      case BluetoothAdapter.STATE_TURNING_OFF: return "STATE_TURNING_OFF";
      default: return "UNKNOWN_STATE_" + state;
    }
  }

  // Helper method to convert audio device types to readable strings
  private String getAudioDeviceTypeString(int type) {
    switch (type) {
      case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP: return "TYPE_BLUETOOTH_A2DP";
      case AudioDeviceInfo.TYPE_BLUETOOTH_SCO: return "TYPE_BLUETOOTH_SCO";
      case AudioDeviceInfo.TYPE_BLE_HEADSET: return "TYPE_BLE_HEADSET";
      case AudioDeviceInfo.TYPE_WIRED_HEADPHONES: return "TYPE_WIRED_HEADPHONES";
      case AudioDeviceInfo.TYPE_WIRED_HEADSET: return "TYPE_WIRED_HEADSET";
      case AudioDeviceInfo.TYPE_USB_HEADSET: return "TYPE_USB_HEADSET";
      default: return "TYPE_UNKNOWN_" + type;
    }
  }
}
