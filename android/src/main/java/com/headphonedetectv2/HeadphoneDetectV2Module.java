package com.headphonedetectv2;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
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

import androidx.annotation.NonNull;

@ReactModule(name = HeadphoneDetectV2Module.NAME)
public class HeadphoneDetectV2Module extends ReactContextBaseJavaModule implements LifecycleEventListener {
  public static final String NAME = "HeadphoneDetectV2";

  private static final String AUDIO_DEVICE_CHANGED_NOTIFICATION = "AUDIO_DEVICE_CHANGED_NOTIFICATION";
  private BroadcastReceiver receiver;

  public HeadphoneDetectV2Module(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  @NonNull
  public String getName() {
    return NAME;
  }


  private void maybeRegisterReceiver() {
    final ReactApplicationContext reactContext = getReactApplicationContext();

    if (receiver != null) {
      return;
    }

    receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        WritableMap res = isAudioDeviceConnected();

        switch (action) {
          case BluetoothDevice.ACTION_ACL_CONNECTED:
            res.putBoolean("bluetooth", true);
            break;
          case BluetoothDevice.ACTION_ACL_DISCONNECTED:
            res.putBoolean("bluetooth", false);
            break;
          default:
            break;
        }

        sendEvent(reactContext, AUDIO_DEVICE_CHANGED_NOTIFICATION, res);
      }
    };

    reactContext.registerReceiver(receiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
    reactContext.registerReceiver(receiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      reactContext.registerReceiver(receiver, new IntentFilter(BluetoothAdapter.EXTRA_STATE), Context.RECEIVER_NOT_EXPORTED);
    }
    reactContext.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
    reactContext.registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
  }

  private void maybeUnregisterReceiver() {
    if (receiver == null) {
      return;
    }
    getReactApplicationContext().unregisterReceiver(receiver);
    receiver = null;
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

      AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
      for (AudioDeviceInfo device : devices) {
          if (
                  device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
                          device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
                          device.getType() == AudioDeviceInfo.TYPE_USB_HEADSET
          ) {
              res.put("audioJack", true);
          }

          if (
                  device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                          device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
          ) {
              res.put("bluetooth", true);
          }
      }

      WritableMap map = new WritableNativeMap();
    for (Map.Entry<String, Boolean> entry : res.entrySet()) {
        map.putBoolean(entry.getKey(), entry.getValue());
    }
    return map;
  }

  private void sendEvent(ReactApplicationContext reactContext, String eventName, @Nullable WritableMap params) {
    reactContext
        .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
        .emit(eventName, params);
  }

  @ReactMethod
  public void isAudioDeviceConnected(final Promise promise) {
      promise.resolve(isAudioDeviceConnected());
  }

  @Override
  public void initialize() {

    getReactApplicationContext().addLifecycleEventListener(this);
    maybeRegisterReceiver();
  }

  @Override
  public void onHostResume() {
    maybeRegisterReceiver();
  }

  @Override
  public void onHostPause() {
    maybeUnregisterReceiver();
  }

  @Override
  public void onHostDestroy() {
    maybeUnregisterReceiver();
  }
}
