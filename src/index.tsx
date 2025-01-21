import HeadphoneDetectV2, {
  AUDIO_DEVICE_CHANGED_NOTIFICATION,
} from './NativeHeadphoneDetectV2';

import { NativeEventEmitter, type EventSubscription } from 'react-native';

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
  return eventEmitter.addListener(AUDIO_DEVICE_CHANGED_NOTIFICATION, callback);
}
