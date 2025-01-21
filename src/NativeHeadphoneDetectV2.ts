import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
import type { ConnectedResult } from 'react-native-headphone-detect-v2';

export interface Spec extends TurboModule {
  // Methods
  isAudioDeviceConnected(): Promise<ConnectedResult>;

  // Event Emitter Methods
  addListener(eventName: string): void;
  removeListeners(count: number): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('HeadphoneDetectV2');

export const AUDIO_DEVICE_CHANGED_NOTIFICATION =
  'AUDIO_DEVICE_CHANGED_NOTIFICATION';
