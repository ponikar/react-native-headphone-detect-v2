import type { TurboModule } from 'react-native';
import { TurboModuleRegistry } from 'react-native';
import type { ConnectedResult } from '../lib/typescript/src';


export interface Spec extends TurboModule {
  // Methods
  isAudioDeviceConnected(): Promise<ConnectedResult>;

  // Event Emitter Methods
  addListener(eventName: string): void;
  removeListeners(count: number): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('HeadphoneDetectV2');