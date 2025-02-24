# React Native Headphone Detection V2

This library is ported from the legacy `react-native-headphone-detection` package with support for TypeScript and the latest versions of Android and iOS.

### Installation

Note that the term `detection` has been shortened to `detect` in the package name.

#### npm

```shell
npm install react-native-headphone-detect-v2
```

#### yarn

```shell
yarn add react-native-headphone-detect-v2
```

### iOS

```shell
cd ios && pod install
```

### Expo

> This library won't work with the Expo Go app. You have to use the [Expo development build](https://docs.expo.dev/develop/development-builds/introduction/) to make it work.

Once you install this library, run the `expo prebuild` command to sync the `android` and `ios` folders.

### API Usage

The `isAudioDeviceConnected()` function returns a promise for a `ConnectedResult`, which indicates whether there is any external `audioJack` or `bluetooth` device connected.

```ts
import { isAudioDeviceConnected } from 'react-native-headphone-detect-v2';

// Both audioJack and bluetooth are boolean values
const { audioJack, bluetooth } = await isAudioDeviceConnected();
```

The `onAudioDeviceChanged` listener detects whether the external audio output device has been changed or not. This function returns a callback to remove the listener, which can be called inside the cleanup function of `useEffect`. Note that this won't work in Android or iOS simulators.

```ts
import { onAudioDeviceChanged } from 'react-native-headphone-detect-v2';
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

### New Architecture Support

This library does not currently support the new architecture. Any pull requests related to this functionality would be appreciated.

### Contribution

- If you have any suggestions or feature requests, you can create an issue accordingly. This library is solely maintained by [ponikar](https://github.com/ponikar). Any pull requests are appreciated.

- This library is based on `react-native-headphone-detection` and it has been ported to `create-react-native-library` for better maintenance and support. Huge thank you to [Tintef](https://github.com/Tintef), the author of `react-native-headphone-detection`.
