package com.headphonedetectv2

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.util.HashMap
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.util.Log

@ReactModule(name = HeadphoneDetectV2Module.NAME)
class HeadphoneDetectV2Module(reactContext: ReactApplicationContext) :
    NativeHeadphoneDetectV2Spec(reactContext) {

    private var receiver: BroadcastReceiver? = null

    private fun maybeRegisterReceiver() {
        val reactContext = reactApplicationContext

        if (receiver != null) {
            Log.d(TAG, "Receiver already registered, skipping registration")
            return
        }

        Log.d(TAG, "Creating new broadcast receiver")
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                Log.d(TAG, "Received action: $action")

                action?.let {
                    when (it) {
                        BluetoothDevice.ACTION_ACL_CONNECTED -> Log.d(TAG, "Bluetooth ACL Connected")
                        BluetoothDevice.ACTION_ACL_DISCONNECTED -> Log.d(TAG, "Bluetooth ACL Disconnected")
                        BluetoothAdapter.ACTION_STATE_CHANGED -> {
                            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                            Log.d(TAG, "Bluetooth Adapter State Changed: ${getBluetoothStateString(state)}")
                        }
                        BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED -> Log.d(TAG, "Bluetooth Connection State Changed")
                        AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> Log.d(TAG, "SCO Audio State Updated")
                        AudioManager.ACTION_AUDIO_BECOMING_NOISY -> Log.d(TAG, "Audio Becoming Noisy")
                        Intent.ACTION_HEADSET_PLUG -> {
                            val headsetState = intent.getIntExtra("state", -1)
                            Log.d(TAG, "Headset Plug State Changed: $headsetState")
                        }
                        else -> Log.d(TAG, "Unhandled action: $action")
                    }

                    val updatedState = isAudioDeviceConnected()
                    Log.d(TAG, "Updated device state - Bluetooth: ${updatedState.getBoolean("bluetooth")}, " +
                            "AudioJack: ${updatedState.getBoolean("audioJack")}")
                    sendEvent(reactContext, AUDIO_DEVICE_CHANGED_NOTIFICATION, updatedState)
                }
            }
        }

        Log.d(TAG, "Registering intent filters")
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_HEADSET_PLUG)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "Registering receiver with RECEIVER_NOT_EXPORTED flag")
                reactContext.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                Log.d(TAG, "Registering receiver without flags")
                reactContext.registerReceiver(receiver, filter)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error registering receiver: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun maybeUnregisterReceiver() {
        if (receiver == null) {
            Log.d(TAG, "No receiver to unregister")
            return
        }

        try {
            Log.d(TAG, "Unregistering receiver")
            reactApplicationContext.unregisterReceiver(receiver)
            receiver = null
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun getConstants(): Map<String, Any> {
        return hashMapOf(AUDIO_DEVICE_CHANGED_NOTIFICATION to AUDIO_DEVICE_CHANGED_NOTIFICATION)
    }

    private fun isAudioDeviceConnected(): WritableMap {
        val res = HashMap<String, Boolean>()
        val audioManager = reactApplicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        res["audioJack"] = false
        res["bluetooth"] = false

        Log.d(TAG, "Checking audio device connections")

        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val bluetoothEnabled = bluetoothAdapter?.isEnabled == true
        Log.d(TAG, "Bluetooth Adapter enabled: $bluetoothEnabled")

        if (bluetoothEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
                Log.d(TAG, "Found ${devices.size} audio devices")

                devices.forEach { device ->
                    val type = device.type
                    Log.d(TAG, "Checking device type: ${getAudioDeviceTypeString(type)}")

                    when (type) {
                        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                        AudioDeviceInfo.TYPE_WIRED_HEADSET,
                        AudioDeviceInfo.TYPE_USB_HEADSET -> {
                            res["audioJack"] = true
                            Log.d(TAG, "Wired audio device detected")
                        }
                        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                        AudioDeviceInfo.TYPE_BLE_HEADSET -> {
                            res["bluetooth"] = true
                            Log.d(TAG, "Bluetooth audio device detected")
                        }
                    }
                }
            } else {
                val isBluetoothConnected = audioManager.isBluetoothA2dpOn || audioManager.isBluetoothScoOn
                res["bluetooth"] = isBluetoothConnected
                Log.d(TAG, "Legacy Bluetooth check result: $isBluetoothConnected")
            }
        }

        return WritableNativeMap().apply {
            res.forEach { (key, value) ->
                putBoolean(key, value)
            }
        }
    }

    private fun sendEvent(
        reactContext: ReactApplicationContext,
        eventName: String,
        params: WritableMap?
    ) {
        Log.d(TAG, "Sending event: $eventName with params: $params")
        reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
    }

    override fun isAudioDeviceConnected(promise: Promise) {
        Log.d(TAG, "isAudioDeviceConnected method called")
        promise.resolve(isAudioDeviceConnected())
    }

    override fun getName(): String = NAME

    override fun initialize() {
        Log.d(TAG, "Initializing module")
        maybeRegisterReceiver()
    }

    override fun addListener(eventName: String) {
        // Required for RN built in Event Emitter Calls
    }

    override fun removeListeners(count: Double) {
        // Required for RN built in Event Emitter Calls
    }

    private fun getBluetoothStateString(state: Int): String = when (state) {
        BluetoothAdapter.STATE_OFF -> "STATE_OFF"
        BluetoothAdapter.STATE_TURNING_ON -> "STATE_TURNING_ON"
        BluetoothAdapter.STATE_ON -> "STATE_ON"
        BluetoothAdapter.STATE_TURNING_OFF -> "STATE_TURNING_OFF"
        else -> "UNKNOWN_STATE_$state"
    }

    private fun getAudioDeviceTypeString(type: Int): String = when (type) {
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "TYPE_BLUETOOTH_A2DP"
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "TYPE_BLUETOOTH_SCO"
        AudioDeviceInfo.TYPE_BLE_HEADSET -> "TYPE_BLE_HEADSET"
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "TYPE_WIRED_HEADPHONES"
        AudioDeviceInfo.TYPE_WIRED_HEADSET -> "TYPE_WIRED_HEADSET"
        AudioDeviceInfo.TYPE_USB_HEADSET -> "TYPE_USB_HEADSET"
        else -> "TYPE_UNKNOWN_$type"
    }

    companion object {
        const val NAME = "HeadphoneDetectV2"
        private const val TAG = "HeadphoneDetectV2"
        private const val AUDIO_DEVICE_CHANGED_NOTIFICATION = "AUDIO_DEVICE_CHANGED_NOTIFICATION"
    }
}