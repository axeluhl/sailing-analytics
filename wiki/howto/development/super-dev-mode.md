# Super Dev Mode

## What is Super Dev Mode?

Modern browsers (Current versions of Chrome/FireFox and IE11+) support so called source maps.
Source maps are used to map generated JavaScript code back to it's original source.
For GWT this means, the JavaScript output of the GWT compiler is mapped back to the original Java Source code.
This enables the browser to run JavaSript while showing Java code in the developer tools/debugger.
This is implemented by GWT as Super Dev Mode (SDM).

## How to use Super Dev Mode?

Super Dev Mode is the default when using GWT 2.7. The project has a committed launch configuration "SailingGWT sdm.launch" that starts Super Dev Mode instead of Dev Mode (launch configuration "SailingGWT.launch").
When starting this configuration it will take much more time that the startup of classic Dev Mode but it will have much better performance later.
When opening an entry point page you will see a popup while the Java code is compiled to JavaScript on the fly. This will also take some time.
When changing some frontend code the compile will be much faster on refresh, as Super Dev Mode incrementally compiles only what changed.

You can add "GWT.debugger();" statements to your Java code.
This will force the JavaScript debugger to stop at this statement.
So you don't have to manually open the file in the dev tools and set a break point.

In addition, there's a composite launch configuration "SailingGWT All SDM.launch" that will start SDM for the SailingGWT modules as well as the Security UI modules.

## Using Eclipse debugger for GWT SuperDevMode

Debugging Java Code in the browser is very inconvenient (e.g. you have to manually open the files you already opened in Eclipse).
Using Using Eclipse debugger for GWT SuperDevMode [[SDBG|http://sdbg.github.io/]] you can use Eclipse to debug SDM code running in Chrome. We have created an update site that does daily checks for new master commits on the Github project. So, for the couraged among you, use http://p2.sapsailing.com/p2/sdbg as your update site. All others will probably want to use http://sdbg.github.io/p2.

### Starting a SDM debug session in Eclipse

To start SDM debugging in Eclipse, do the following:

* Start the backend as you would always do.
* Start GWT Super Dev Mode by launching "SailingGWT sdm.launch" or "SailingGWT All SDM.launch".
* Do not Click on the URLs provided in "Development view"!
* Run "Debug SailingGWT SDM on Chrome.launch"
* A new instance of Chrome is started and you will see a debug session running in Eclipse
* Your Eclipse break points will now work in the running debug session.
* You are free to change the URL or navigate to other pages in the automatically openend browser window (debugging will go on)

### Noteworthy information & Troubleshooting

If you are faced with an error stating that your Chrome installation isn't found, do the following:

* Edit your "eclipse.ini" file
* Add a new line: -Dchrome.location=<PATH/TO/CHROME/BINARY>
  * This line must be added after "-vmargs"
  * There must not be any quotation marks around the path (even if there are whitespaces in the path)
* Save the file and restart Eclipse

Be aware that some of the Eclipse debugger features do NOT work with this kind of debug session or work differently:

* Logical views of certain data structures (e.g. Collections) aren't supported
* Exception break points do not work (use a break point in the specific Exception constructor instead)
* Variable naming is not 1:1 what you expect in the Java world
* Stacks look different but clicking on Stack elements should
* [[Stack Traces logged in Chrome do not use source maps|https://code.google.com/p/chromium/issues/detail?id=357958]], so you see the JavaScript Stack Trace instead.

## Debugging SDM on Android devices

### Requirements

To enable [[https://developer.chrome.com/devtools/docs/remote-debugging|remote debugging for your Android device]] do the following steps:

* Go to the System settings
* If you do not see the "Developer options" menu, go to "About the device" and tap multiple times on "Build number" until a message appears
* Go to the "Developer options" menu 
* Activate "USB debugging"
* A notification apears in the system notification center while USB-debugging is turned on

Other requirements:

* You need Android 4.0+ to make remote debugging work
* You need a recent version of Chrome installed on both, the Android device and your development system

To be able to discover your Chrome browser running on the Android device from your development system you need to do the following:

* Connect your device with USB-debugging turned on to your development system
* Open Chrome on the Android device

### Debugging via Desktop Chrome

Do the following to start a debugging session via Chrome installed on your development system:

* Open Chrome on your development system and navigate to [[chrome://inspect/#devices]]
* Only the first time you need to configure the needed ports:
  * Check "Discover USB Devices
  * Click the button "Port forwarding ..."
  * Add an Entry for Port "8888" for address "localhost:8888"
  * Add an Entry for Port "9876" for address "localhost:9876"
  * Close the dialog
* Enter the URL (e.g. [[http://localhost:8888/gwt/Home.html]]) nearby the connected Chrome browser of your device and press open
* The specified URL will be opended in a new tab on your android device
* A new window with an instance of the developer tools will appear on the development system
  * If it doesn't open automatically press "inspect" for the browser tab
* You can now go to "Sources" and debug the source-mapped JS source as it is a local browser tab

### Debugging via SDBG

To debug SDM on your Android Chrome via SDBG do the following:

* Get to know your Device ID
  * Navigate to [[chrome://inspect/#devices]] using your desktop Chrome while the device is connected with USB-Debugging turned on
  * The Device ID is shown next to the device name
  * Take the Device ID without the prefixed "#"
* Copy the launch configuration templates "Debug SailingGWT SDM on Android.launch.template" and "Forward SailingGWT Ports for Android.launch.template" as *.launch files
* Replace <YOUR-DEVICE> with your concrete Device ID
* Launch "Forward SailingGWT Ports for Android.launch"
* Launch "Debug SailingGWT SDM on Android.launch"
* You should now be able to debug in Eclipse as described above
