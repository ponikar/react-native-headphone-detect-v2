import {
  NativeModules,
  Platform,
  NativeEventEmitter,
  type EventSubscription,
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-headphone-detect-v2' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const HeadphoneDetectV2 = NativeModules.HeadphoneDetectV2
  ? NativeModules.HeadphoneDetectV2
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

const eventEmitter = new NativeEventEmitter(HeadphoneDetectV2);

export type ConnectedResult = {
  audioJack: boolean;
  bluetooth: boolean;
};

export function isAudioDeviceConnected(): Promise<ConnectedResult> {
  return HeadphoneDetectV2.isAudioDeviceConnected();
}

export function onAudioDeviceChanged(
  callback: (p: ConnectedResult) => void
): EventSubscription {
  return eventEmitter.addListener(
    HeadphoneDetectV2.AUDIO_DEVICE_CHANGED_NOTIFICATION,
    callback
  );
}
