# React Native Headphone Detection V2

- This library is ported from the legacy `react-native-headphone-detection` library with support for the latest version of Android/iOS SDK, and Typescript intellisense.

### Installation

#### npm

```shell
npm install react-native-headphone-detection-v2
```

#### yarn

```shell
yarn add react-native-headphone-detection-v2
```

### iOS installation

```shell
cd ios && pod install
```

### Expo

> This library won't work with Expo go app. You have to use the expo [development build](https://docs.expo.dev/develop/development-builds/introduction/) to make it work.

Once you install this library run the `expo prebuild` command to sync native ios/android folders.

### API usage

- `isAudioDeviceConnected()` returns a promise to determine whether there is any external `audioJack` or `bluetooth` device connected.

```javascript
import { isAudioDeviceConnected } from 'react-native-headphone-detection-v2';

// both audioJack and bluetooth are boolean values
const { audioJack, bluetooth } = await isAudioDeviceConnected();
```

- `onAudioDeviceChanged` detects whether the external audio output device has been changed or not.
  This function returns a callback to remove that listener. Usually, you have to call it inside the cleanup function of `useEffect`

```javascript
import { onAudioDeviceChanged } from 'react-native-headphone-detection-v2';
import { useEffect } from 'react';

useEffect(() => {
  const { remove } = onAudioDeviceChanged(({ audioJack, bluetooth }) =>
    console.log('AUDIO DEVICE CHANGED')
  );
  return () => {
    remove();
  };
}, []);
```

> This won't work for android/ios simulators.

### New Architecture support

- For now, it doesn't have support for that. I shall add that in the future. Any PR regarding that would be appreciated.

### Contribution

- If you have any suggestions or feature requests, you can create an issue accordingly. This library is solely maintained by [ponikar](https://github.com/ponikar). Any PR regarding improvements or features would be appreciated.

- As I said earlier the core library is `react-native-headphone-detection` and I have ported it to `create-react-native-library` for better maintenance and support.

Huge thank you to the author [Tintef](https://github.com/Tintef) of `react-native-headphone-detection`.
