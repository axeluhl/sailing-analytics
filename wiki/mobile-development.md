# Mobile Development

The native Android projects are in the mobile/ folder in git which is next to the java/ folder. To build them successfully, you need to install the Android SDK which is available [here](http://developer.android.com/sdk/index.html). Also, you need to use the Eclipse update site https://dl-ssl.google.com/android/eclipse and install the Eclipse ADT plugins.

Currently (2013-07-18), the Race Committee App uses the Google APIs Version 13 (Android 3.2) which you need to install through the Android SDK Manager (see Eclipse toolbar after installing the Eclipse ADT plugin). In the Android Virtual Device manager which can also be found in the Eclipse toolbar you can configure an emulated device. One successful approach was using a 10.1" WXGA (Tablet) device in the emulator configuration and choose "Google APIs (Google Inc.) - API Level 13" as the target.

After that it should be possible to choose "Debug as --> Android application" in the com.sap.sailing.racecommittee.app project's context menu, then pick the emulator you're previously created.

If you want to run the app against your locally-running server, go into the Settings and choose http://10.0.2.2:8888 as the JSON URL. See also [here](http://developer.android.com/tools/devices/emulator.html#emulatornetworking) for more details on the emulator's network behavior.