# Mobile Development

See [Onboarding](/wiki/howto/onboarding#Additional-steps-required-for-Android-projects) how to set up your build environment for mobile development.

## Emulation

Besides running the application on a plugged-in device there are multiple options for using an emulator:

* Android Virtual Device (AVD)
 * Default Android emulator
 * Comes with the ADT and is well integrated into Eclipse
 * In the AVD manager which can be found in the Eclipse toolbar you can configure an emulated device. Create a 10.1" WXGA (Tablet) device in the emulator configuration and choose "Google APIs (Google Inc.) - API Level 13" as the target.
 * Use virtual device as described in [[Onboarding|wiki/howto/onboarding]]
 * If you want to run the app against your locally-running server, go into the Settings and choose http://10.0.2.2:8888 as the JSON URL. See also [here](http://developer.android.com/tools/devices/emulator.html#emulatornetworking) for more details on the emulator's network behavior.
* Genymotion (AndroVM)
 * VirtualBox-based Android emulator
 * Currently in free beta phase; way faster than AVD; better support for Google Apps (e.g. Maps) and easy access to sensor features (e.g. setting GPS info)
 * Register at http://www.genymotion.com/, download and install virtual device "WXGA 10.1 Tablet - 4.1.1 - with Google Apps - API 16 - 1280x800" with 160dpi
 * If you want to run the app against your locally-running server, check the IP address of your host machine for the VirtualBox network interface. Use this IP when you are configuring the app.
 * Use virtual device as described in [[Onboarding|wiki/howto/onboarding]]

## Mocking the server

For rapid development of the mobile applications using a mocked server has proven to work best. One can use [Fiddler](http://fiddler2.com/)'s AutoResponder feature to quickly try out new mobile features and easily test some edge cases.

## Android Studio

At the time of writing there is no way to develop in Eclipse and Android Studio simultaneously. This due to different project file formats and expected project directory structure. Although Android Studio will be the number one platform for Android development in the future, currently it is not feasible to switch, because of

* Android Studio's build system Gradle is still in beta
* OSGi support in the free version of Android Studio is far from completed
* Android Studio is in Alpha (December 2013)