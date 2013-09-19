# UI Tests with Selenium

[[_TOC_]]

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

## Writing and Execution of Tests

**TODO by Riccardo**