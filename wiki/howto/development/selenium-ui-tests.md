# UI Tests with Selenium

[[_TOC_]]

## Quick Start: How to run tests locally in Eclipse

There are two ways to run the Selenium tests locally on your computer. Either, you compile the GWT UI using our build script and run the tests based on the compiled UI, or you run the tests using the GWT super dev mode.

### Firefox Prerequisites

Running Selenium tests with FireFox requires to use GeckoDriver. You need to download a current version of GeckoDriver from [the download page](https://github.com/mozilla/geckodriver/releases) and unzip it on your system. At the time this was written, the current version was 0.21.0 and it worked best with FireFox versions 57-61 but other versions of both, GeckoDriver and FireFox should also work. In the file "/com.sap.sailing.selenium.test/local-test-environment.xml" you need to adjust the property "webdriver.gecko.driver" to point to the unzipped GeckoDriver executable. If you have a version of FixeFox globally installed on your system, GeckoDriver will use this one. If you do not have an installed version of FireFox or need to use a different version, you can set the property "webdriver.firefox.bin" in "/com.sap.sailing.selenium.test/local-test-environment.xml" to point to the specific firefox executable. When using a portable version, it is not the "FirefoxPortable.exe" but "App/Firefox/firefox.exe".

In older versions of Selenium, you needed to configure a special user profile in FireFox. With GeckoDriver, this isn't needed anymore.

### Running the tests with GWT super dev mode

Launch the server by choosing the "Sailing Server (Proxy, winddbTest)" or "Sailing Server (No Proxy, winddbTest)" launch config. Then, run the "GWT Sailing SDM" launch to start the GWT UI in super dev mode (SDM).

When the GWT SDM has finished its initialization as indicated by the "Development Mode" view showing the entry point URLs, launch the "com.sap.sailing.senelium.test (Proxy)" or "com.sap.sailing.senelium.test (No Proxy)" launch. This will then pop up Firefox and run the tests.

### Running the tests after a successful local GWT compile

Stop any SailingServers currently running on your local machine (if you fail to do so, the Maven build will be unable to write some of the build artifacts in the _com.sap.sailing.gwt.ui_ project). Build using the git-managed build script from _configuration/buildAndUpdateProduct.sh_. To lower round-trip times, we introduced the _-b_ option that only builds one GWT permuation (Chrome, English). You may use the _-t_ option to keep the Maven build from running the tests. If inside the SAP VPN, additionally use the _-p_ option to ensure the HTTP proxy is being used. This makes for a command line like this:

  `./configuration/buildAndUpdateProduct.sh -p -b -t build`

After the build has completed (hopefully successfully), refresh the `com.sap.sailing.gwt.ui` project in your Eclipse workspace. Start the SailingServer launch configuration (with / without proxy, depending on your VPN state). Then, launch the JUnit launch configuration _com.sap.sailing.selenium.test_ (with / without proxy). It will run the Selenium tests against your local server, running on port 8888. Make sure you have a "Selenium" firefox profile installed, see above.

## General

To validate the web application from the user perspective we use a combination consisting of Selenium and JUnit, which allows us to simulate user interactions via a real browser instance. Therefore we provide a special JUnit-runner as well as different classes in the package _com.sap.sailing.selenium.core_ to execute tests, using the Selenium WebDriver API, and to keep the execution of the tests as flexible as possible at the same time.

To avoid duplicated and error prone code in test cases, we prefer the usage of the Page Object design pattern when writing automated UI tests. A page object is an object-oriented class that serves as an interface to the UI. The tests then use the methods of the page object class whenever they need to interact with the UI. The benefit is that if the UI changes, the tests themselves don’t need to be changed. Only the code within the page object needs to be changed. Subsequently all changes to support the new UI are located in one place.

## Selenium Runner

The Selenium runner is used to execute tests, using the Selenium WebDriver API. The runner takes a XML configuration file, which has to be provided via the system property _selenium.test.environment.configuration_, and runs the tests for each defined browser. The configuration file is needed to define the browsers with which the tests should be executed as well as to provide all the information to Selenium regarding to the web drivers. To avoid unnecessary errors we also provide a documented XML Schema for the configuration file.

At runtime, the instance of the web driver along with additional information from the configuration is encapsulated by the interface _TestEnvironment_. The Selenium runner provides an instance to the tests by injecting it to fields of the correspondent type, annotated with _Managed_. With the class _AbstractSeleniumTest_ we provide a skeletal implementation for tests, which already has all the necessary annotations applied. Furthermore this class provides a mechanism to automatically capture a screenshot in the case that a test fails.

## Additional Classes

Since GWT applications are heavily AJAX driven, one of the challenges in performing UI tests is to be able to tell when the application is in a state you expect. In simple cases you can wait until a loading animation disappears or until a well-known element has changed the state (e.g. visibility). However, since things are usually not that simple, especially in the context of GWT, it is more stable if we are able to tell exactly when an asynchronous request has finished. For this reason we use a counter for pending request which is incremented if an asynchronous callback is created and decremented when the callback completes, regardless of a success or a failure. With this counter a test which triggers a request (which can cause additional request) can wait until the counter reaches 0 again.

The necessary JavaScript code for the counter is automatically injected in the host page by the provided class _AbstractEntryPoint_. With the class _MarkedAsyncCallback_ we provide an abstract implementation of the _AsyncCallback_ interface for asynchrony requests, which should be marked as pending until they complete using the introduced counter. Since the counter is incremented as soon as an instance is created, _MarkedAsyncCallback_ is an object of single-use. Therefore it is strictly forbidden to create an instance of this class which is never used or which is used multiple times. Otherwise the counter will have an unpredictable value.

## Page Object Pattern

Within a web app's UI there are areas that tests interact with. A page object simply models these as objects within the test code and abstracts from the actual structure (DOM) of a website. This reduces the amount of duplicated code and means that if the UI changes, the fix need only be applied in one place.

Page objects can be thought of as facing in two directions simultaneously. Facing towards the developer of a test, they represent the services offered by a particular page. Facing away from the developer, they should be the only thing that has a deep knowledge of the structure of a page (or part of a page). It's simplest to think of the methods on a page object as offering the "services" that a page offers rather than exposing the details and mechanics of the page.

Because the developer of a test should think about the services that they're interacting with rather than the implementation, page objects should seldom expose the underlying web driver or elements. To facilitate this, methods on a page object should return other page objects.

Since we prefer the usage of the Page Object design pattern when writing automated UI tests, we provide a base class _PageObject_ as well as the two specializations _HostPage_ and _PageArea_. The class _HostPage_ represents a GWT entry point which waits for the completion of the GWT bootstrap process before it is initialized. The _PageArea_ is used to represent a component or a specific area on a page, which is useful to improve maintainability for complex websites and reusable parts.

There is a lot of flexibility in how the page objects may be designed, but there are a few basic rules for getting the desired maintainability of the test code. Page objects themselves should never be make verifications or assertions. This is part of the test and should always be within the test’s code, never in a page object. The page object will contain the representation of the page, and the services the page provides via methods but no code related to what is being tested should be within the page object. There is one, single, verification which can, and should, be within the page object and that is to verify that the page or the area, and possibly critical elements, were loaded correctly. This verification should be done while instantiating the page object.

To make the development of page objects as simple and easy as possible, we also support a factory for this pattern which helps to remove some boiler-plate code from the page objects by using annotations. We provide the annotations _FindBy_ and _FindBys_ for fields on a page object to specify a mechanism for locating an element or a list of elements. The factory uses the annotations to lazily locate and wait for the element or the element list to appear, by polling the UI on a regular basis, and initializes the field with the element or the list of elements.

Since we use GWT to build the UI, we also provide a specific mechanism to locate elements by the value of the GWT debug identifier. By convention we use the attribute _selenium-id_ for the debug identifier which is implemented by the class _AbstractEntryPoint_. The class _BySeleniumId_ defines the corresponding mechanism and locates an element or a list of elements by the value of the selenium-id attribute.

## Writing new Tests

If you start to write new UI tests, some preparations are needed, depending on what you work on. The first thing you should verify is, that the used entry point extends the provided class _AbstractEntryPoint_, since this class automatically injects the JavaScript code for the counter of pending request into the host page. In addition it configures the attribute used for the debug identifier and marks the GWT bootstrap using the counter. Since the counter is initialized to be 1 and decremented as soon as the bootstrap process has finished, tests as well as page objects are able to wait until the bootstrap process completes.

In the next step you should set a debug identifier for all relevant widgets the user interacts with, by calling _ensureDebugId(String id)_ on the instance of the widget. This will set the _selenium-id_ attribute of the widget to the specified value, so you can easily look up it in the resulting HTML document. You should also consider setting a debug id on logical or self-contained parts, like a form or field sets. By convention we always use the type of the widget as a suffix (e.g. _LoginButton_ or _NameTextField_) to give a hint to the available interactions.

After preparing the UI for testing, you have to implement new page objects or to extend existing ones, if needed. Therefore we provide the two classes _HostPage_ and _PageArea_ which also share common functionality. The class _HostPage_ represents a whole page and in the context of GWT it is used for page objects representing an _EntryPoint_. The second class _PageArea_ in contrast should be used to represent complex widgets (e.g. tables and tab panels) or logical parts like field sets. While implementing your page objects, you should always think in services instead of single widgets. So, for example you should provide a method _login(String name, String password)_ for a simple login form, which sets the username, the password and clicks the login button instead of the 3 methods _setName(String name)_, _setPassword(String password)_ and _login()_.

To get access to the HTML-elements representing the widgets the user interacts with, you should use the provided factory by creating instance fields of the type _WebElement_ or _List\<WebElement\>_ and annotating them with _FindBy_ or _FindBys_ as well as _CacheLookup_, if the element is static and never changes. Usually you use the annotation _FindBy_ which needs you to provide the mechanism as well as the value to use for locating the element. Since we use the debug identifiers of GWT we also provide the corresponding mechanism with the class _BySeleniumId_ and a typical example would look like following:

        public MyPageObject extends PageArea {
            @FindBy(how = BySeleniumId.class, using = ”NameTextField”)
            private WebElement nameTextField;
            
            ...
        }

Since GWT widgets are usually represented by more complex HTML constructs it is also recommended to implement a page object for the used widgets, if none exists. A _CheckBox_ for example is modeled by the following HTML fragment:

        <span class=”gwt-CheckBox” style=”white-space: nowrap”>
            <input id=”gtw-uid-1” type=”checkbox” value=”on”/>
            <label for=”gwt-uid-1”>Track Wind</label>
        </span>

The corresponding page object should abstract from this by only providing methods to select and deselect the checkbox as well as to retrieve the selection state. The knowledge about the lookup of the input field should be hidden by the implementation. This allows a reuse in all tests and other page objects and keeps necessary changes in one place for the case GWT modifies the representation in later releases.

To write a test case you should extend the class _AbstractSeleniumTest_ which has all necessary annotations already applied for running the test with the special JUnit-Runner and to have access to the _TestEnvironment_ which provides the _WebDriver_ instance as well as some other configuration settings. The concrete test is then written as usual, using the services of the page objects and making assertions.


## Execution of Tests

For the execution of the tests you have several options. In either case you have to provide a configuration file according to your environment, since our tests are run by the special JUnit-Runner. You can use the file _ci-test-environment.xml_ as a starting point for your own configuration. Within the configuration file you define the context root to specify the server and the path of the web application. Furthermore you have to list all browsers via _driver-definition_ elements you want to test with.

The easiest way to execute the tests is to perform a full Maven build. Here you have to provide the configuration file in the property with the name parameters.integration-tests as a command line argument in your user settings.

        <properties>
            <parameters.integration-tests>
                -Dselenium.test.environment.configuration=[path-to-your-file]
            </parameters.integration-tests>
        </properties>

After the build, Tycho will start a server instance and runs all tests against the deployed application.

Since a full Maven build needs some time, you can also execute the tests in the Eclipse IDE. Here you have to start a server manually via an appropriated launch configuration (e.g. _SailingServer (Proxy, winddbTest)_). From there you can either run all UI-Tests with the JUnit launch configuration _com.sap.sailing.selenium.test_ (with/without proxy) which expects the configuration file under the name _local-test-environment.xml_ or you can run a single test by selecting _Run As -> JUnit Test_ for your test class and specifying the configuration file in the _VM Arguments_ section of the run configuration.

For a more practical example of how to write page objects and test you should take a look at the [[tutorial|wiki/howto/misc/ui-tests-tutorial]].

## Running Selenium tests on IE11

You need IEDriverServer.exe to successfully run selenium tests using IE 11. The download is listed on the official Selenium download page: https://www.seleniumhq.org/download/

In local-test-environment.xml you need to set the property "webdriver.ie.driver"  to point to the downloaded and unzipped IEDriverServer.exe, not the ie.exe!
       
With IE 10/11, only the 32Bit version works by the time of writing.
       
Some further configuration steps may be required depending on the Windows version:
https://github.com/SeleniumHQ/selenium/wiki/InternetExplorerDriver#required-configuration

In case you see test cases fail with a java.net.BindException with message "Address already in use: connect" you can tune Windows' TCP settings via registry (as admin). In "Computer\HKEY_LOCAL_MACHINE\SYSTEM\CurrentControlSet\Services\Tcpip\Parameters" set the following values:

* "MaxUserPort" to decimal value 65534 (create a DWORD if it does not exist)
* "TcpTimedWaitDelay" to decimal value 30 (create a DWORD if it does not exist)

You need to reboot the system after changing the values above.

## Updating Selenium

While our build environment is stable regarding to the used browser version, this may not be the case in your development environment, where you have the newest browser version installed probably. The short release cycles of the browsers often bring changes in the implementation, which are incompatible with Selenium. Therefore you have to update the used Selenium version by performing the following steps to be able to run the tests local.

* Download the latest _Client_ for Java from the official [Selenium](http://docs.seleniumhq.org/download/) website
* Delete the _client-combined-<version>.jar_ (plus the _client-combined-<version>-sources.jar_) in the root directory of the project _org.openqa.selenium.osgi_ as well as all libraries in the _lib_ directory and copy the new versions from the downloaded file in  the appropriate folders
* Open the _MANIFEST.MF_ with the Plug-in Manifest Editor and switch to the _Runtime_ tab
    * Remove all the old libraries from the _Classpath_ section and add all new versions
    * Add all new packages that start with _org.openqa.selenium_ to the _Exported Packages_ section in the case there were packages added
* Updated the version number in the _MANIFEST.MF_ and in the _pom.xml_
* update build.properties to reflect the changed *.jar files
* Update the version number in _MANIFEST.MF_ files referencing the _org.openqa.selenium_ bundle (e.g in _com.sap.sailing.selenium.test_)
