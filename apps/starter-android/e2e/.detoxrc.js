/** @type {Detox.DetoxConfig} */
module.exports = {
  testRunner: {
    args: {
      '$0': 'jest',
      config: 'e2e/jest.config.js',
    },
    jest: {
      setupTimeout: 120000,
    },
  },
  apps: {
    'android.debug': {
      type: 'android.apk',
      binaryPath: 'app/build/outputs/apk/dev/debug/app-dev-debug.apk',
      build: './gradlew assembleDevDebug assembleDevDebugAndroidTest',
      reversePorts: [3000],
    },
  },
  devices: {
    emulator: {
      type: 'android.emulator',
      device: {
        avdName: 'Pixel_6_API_34',  // TODO: match your local AVD name
      },
    },
  },
  configurations: {
    'android.emu.debug': {
      device: 'emulator',
      app: 'android.debug',
    },
  },
};
