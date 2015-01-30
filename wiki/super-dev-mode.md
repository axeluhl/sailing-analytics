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

## Using Eclipse debugger for GWT SuperDevMode

Debugging Java Code in the browser is very inconvenient (e.g. you have to manually open the files you already opened in Eclipse).
Using Using Eclipse debugger for GWT SuperDevMode [[SDBG|http://sdbg.github.io/]] you can use Eclipse to debug SDM code running in Chrome. We have created an update site that does daily checks for new master commits on the Github project. So, for the couraged among you, use http://p2.sapsailing.com/p2/sdbg as your update site. All others will probably want to use http://sdbg.github.io/p2.

### Starting a SDM debug session in Eclipse

To start SDM debugging in Eclipse, do the following:

* Start the backend as you would always do.
* Start GWT Super Dev Mode by launching "SailingGWT sdm.launch".
* Do not Click on the URLs provided in "Development view"!
* Copy one of the URLs instead
* Go to Run -> Run Configurations ...
* Create a new configuration of type "Launch Chrome"
* Paste the URL and select the UI project you launched SDM for
* Save and run => A new instance of Chrome is started and you will see a debug session running in Eclipse
* Your Eclipse break points will now work in the running debug session.

The created and save run configuration can be used for subsequent launches.

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
