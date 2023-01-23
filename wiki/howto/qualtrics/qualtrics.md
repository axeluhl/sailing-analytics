# Including Qualtrics Survey in Web Application

This article is deprecated. With bug 5802 we have removed the Qualtrics feedback facility from the solution again.

## Qualtrics Configuration

The Qualtrics configuration should be visible under [https://sapdemo.eu.qualtrics.com/Q/MyProjectsSection](https://sapdemo.eu.qualtrics.com/Q/MyProjectsSection), but sometimes it's not. Sometimes, only the survey itself with its evaluation reports can be seen there. The special link [https://sapdemo.eu.qualtrics.com/siui/#/projects/ZN_7WmsxxHQyCeUivX/creatives/list-view](https://sapdemo.eu.qualtrics.com/siui/#/projects/ZN_7WmsxxHQyCeUivX/creatives/list-view) may help to get to the relevant configuration content. Get in touch with thomas.esser@sap.com for questions around getting an account. See [here](https://www.qualtrics.com/support/website-app-feedback/common-use-cases/single-page-application/) for documentation.

Qualtrics defines three essential types of things:

* Survey
* Creative
* Intercept

They all are grouped in something like a project which is loaded into a web page together with all its elements in one chunk.

A "Survey" defines the set of questions that the user will see when providing feedback. Rules can be defined which question is shown when, and these rules can be dynamic, expanding more content based on previous answers.

A "Creative" is a UI representation use to trigger and/or present the survey. It can be something like a simple button which then fires up another UI element presenting the actual survey content, or it can be the container for the survey directly. Some Creatives can be responsive, showing different content on different device and screen types; others may have timeout rules, etc.

An "Intercept" is a piece of invisible logic that controls when which Creative is displayed to the user to ask for feedback and show the Survey. An Intercept can have one or more "Action Sets", basically defining conditions for a Creative to be shown and linking to the Survey to be presented by the Creative. An Intercept can define whether it will start evaluating its rules upon page loading (which will, according to my experience, only work if the project is not set for "Manually Load Project"), or, e.g., upon explicit JavaScript triggering. See below.

## Qualtrics, the Browser, and JavaScript

In the "Deployment" section of a Qualtric project's "Settings" a JavaScript code snippet is presented for copy&paste into a web page. The snippet is generated to contain the project ID which links it to the set of Surveys, Creatives, and Intercepts defined within the scope of the project. Loading and running this script in a web page can have one of two effects, depending on the "Project Options" that can be configured in the project's "Manage Project" drop-down menu. There, in the "Project Loading Options," you can select the "Manually Load Project" option.

If a project is set to "Manually Load Project" here, the project logic has to be loaded and run explicitly by the web page, using the Qualtrics API's ``load()`` and ``run()`` methods. No Intercept will be evaluated until loading and running the project has taken place.

If a project is *not* set to "Manually Load Project" then it behaves as if ``load()`` and ``run()`` had been called automatically when the page has finished loading.

When "running" the project, all Intercepts will be evaluated, possibly leading to the display of one or more Creatives. At any time, you may invoke the ``unload()`` method, followed by ``load()`` and ``run()`` calls again, to trigger another round of Intercept evaluation. It seems that Intercepts really only evaluate their logic when run, either upon initial load (for projects *not* having "Manually Load Project" set) or explicitly upon ``load()/run()``. Don't expect, for example, a rule for page visit duration to automatically trigger the Intercept when that duration has passed. You would have to periodically call ``run()`` for this.

## Qualtrics and GWT

In order to allow GWT applications to interface with Qualtrics, we introduced the package ``com.sap.sse.gwt.qualtrics`` with its main class ``Qualtrics``. It has a method ``ensureInjected(String projectId)`` method which is currently used by the entry point ``QualtricsInjectingEntryPoint`` loaded by module ``Qualtrics.gwt.xml``. Inherit like this:

```
        <inherits name='com.sap.sse.gwt.Qualtrics'/>
```

It uses a default project ID. If you want your own, define your own module with an entry point class calling ``Qualtrics.ensureInjected(...)`` with a different project ID.

Then, the ``Qualtrics`` class offers a few class-scoped methods to drive the Qualtrics API, including the ``load()``, ``unload()``, and ``run()`` methods which can be used as described above, e.g., in case to trigger Intercepts explicitly if the project has requested "manual" loading, or to trigger Intercepts that are set for explicit JavaScript triggering.

## The ``sapsailing.com`` survey published October 2020

We have prepared a survey for the SAP Sailing Analytics. This survey, project ID ``ZN_7WmsxxHQyCeUivX``, asks users for feedback about the SAP Sailing Analytics (sapsailing.com) in two stages. It is organized as a survey with two Intercepts and two Creatives. The first Intercept requires a JavaScript trigger (invoking the ``load()/run()`` methods) explicitly which is what the "Feedback" link in the web site's footer does now. The Intercept links to a "Responsive Dialog" Creative which then shows the survey in an adequate way on different screen sizes.

The second Intercept points to a "Slider" Creative that displays as a small feedback button on the side of the screen. The Creative has a timeout of currently 30s. Since the Intercept is configured to trigger upon page load, and because the project is not set to "Manually Load Project", the time starts ticking when the page has finished loading. Together with the repeat prevention, based on a Cookie and set for three months, a user who has cookies enabled won't see the survey more than once every three months. And we don't show the survey to users who don't have cookies enabled because we would keep showing it to them over and over again. For the "Slider" it is important to configure the space available for the survey representation such that it also fits smaller phone-sized screens.