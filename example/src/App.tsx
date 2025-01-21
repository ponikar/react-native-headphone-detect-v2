import { useState, useEffect } from 'react';
import { StyleSheet, View, Text } from 'react-native';
import * as HeadPhone from 'react-native-headphone-detect-v2';
export default function App() {
  const [result, setResult] = useState<string | undefined>();

  useEffect(() => {
    (async () => {
      const response = await HeadPhone.isAudioDeviceConnected();

      setResult(JSON.stringify(response));

      HeadPhone.onAudioDeviceChanged((data) => {
        setResult(JSON.stringify(data));
      });
    })();
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
