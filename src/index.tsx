import { NativeModules, Platform, NativeEventEmitter } from 'react-native';

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

type R = {
  audioJack: boolean;
  bluetooth: boolean;
};

export function isAudioDeviceConnected(): Promise<R> {
  return HeadphoneDetectV2.isAudioDeviceConnected();
}

export function onAudioDeviceChanged(callback: (p: R) => void): {
  remove: () => void;
} {
  return eventEmitter.addListener(
    HeadphoneDetectV2.AUDIO_DEVICE_CHANGED_NOTIFICATION,
    callback
  ) as any;
}
