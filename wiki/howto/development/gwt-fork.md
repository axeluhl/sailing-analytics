# Building and Using a Forked GWT Version

If we feel we'd like to change something in GWT that we cannot get into the official GWT release, be it temporarily or for good, we have to build our own version of GWT. For the first time this became relevant in the context of [bug5077](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=5077). The following sections describe the necessary steps to build and work with a forked version of GWT.

## TL;DR

See ``configuration/install-gwt``, adjust the ``GWT_VERSION`` and the repository URLs and branches; make sure that under ``configuration/gwt-poms`` you have the necessary ``pom.xml`` files for the version you're planning to build (usually you can copy an existing file from a different version and simply adjust the version number in the new copy). Also see the script's use in ``configuration/buildAndUpdateProduct.sh`` and read and follow the "Forking the GWT Project" section.

## Forking the GWT Project

See, e.g., [https://github.com/axeluhl/gwt/tree/issue-7987-compatibility](https://github.com/axeluhl/gwt/tree/issue-7987-compatibility) which is a fork of the original Github project at [https://github.com/gwtproject/gwt](https://github.com/gwtproject/gwt). You can follow the instructions in the README of the original project for building instructions. This will ask you to also clone the GWT tools Github repository from [https://github.com/gwtproject/tools/](https://github.com/gwtproject/tools/). Note that when you use an IDE to consolidate your imports then you're likely to cause Lint errors based on extra imports that an IDE would include for Javadoc type references where the import is not used by non-comment code.

Running the build successfully produces a file ``gwt-0.0.0.zip`` (or ``gwt-x.x.x.zip`` if you used the ``-Dgwt.version=x.x.x`` command-line option for your build) in the ``dist`` folder.

Since for a Maven build with the patched version you'll also need a fork of the ``gwt-maven-plugin`` you will also have to fork the [https://github.com/gwtproject/tools/](https://github.com/gwtproject/tools/) repository, adjust the GWT version it uses in the top-level ``pom.xml`` file, adjust the version it builds and comment the dependency on ``gwt-servlet`` and ``requestfactory``. For an example of such a fork see [https://github.com/axeluhl/gwt-maven-plugin/tree/2.11.0](https://github.com/axeluhl/gwt-maven-plugin/tree/2.11.0).

## Using the ``configuration/install-gwt`` Script to Build and Deploy Forked GWT

Adjust the ``GWT_VERSION`` variable in ``configuration/install-gwt`` as well as the repository and branch specifiers at the top of the file, telling the script which repositories / forks to use; then invoke with your local GIT root folder as the first parameter, and optionally as a second parameter a folder where you want the resulting GWT distribution placed as a ZIP file named ``gwt-x.x.x.zip`` with ``x.x.x`` being the version number you specified in ``GWT_VERSION``. For example:

```
    ./configuration/install-gwt /home/me/git-sail /tmp
```

This will create a new, unique temporary folder, clone the GWT fork, the GWT tools, ``gwt-maven-plugin`` fork there, then build the forked GWT version, resulting in a ZIP file named after the version you specified in the ``GWT_VERSION`` variable before. If you specified a folder for this ZIP file as a second parameter, the ZIP file will be copied to that location. The script will then unpack the distribution ZIP file in a temporary folder and copy the JAR files necessary to your GIT workspace into the OSGi GWT wrapper bundles' ``lib/`` folders. It then installs these JAR files as Maven artifacts into your local Maven repository (usually at ``~/.m2/repository``). The POM files necessary for this step are taken from ``configuration/gwt-poms/*-${GWT_VERSION}``, so you have to make sure that those files exist, e.g., by copying them from other versions already available in that folder and only adjusting the version number.

Next, the script builds and installs the ``gwt-maven-plugin`` artifact.

You can then use the ``gwt-x.x.x.zip`` file from the folder you specified to install it as your GWT version of choice in your Eclipse installation (Preferences / GWT / GWT Settings). Refresh your Eclipse workspace to make the IDE recognize the changes in the ``lib/`` folders of the GWT wrapper bundles. Then you need to adjust the ``Bundle-Version:`` line in the ``com.google.gwt.[dev|servlet|user]`` wrapper bundles to your ``GWT_VERSION`` of choice. Furthermore, adjust the ``gwt.version`` property in ``java/pom.xml`` to match the ``GWT_VERSION`` you specified. The latter is relevant for the Maven build to work.

## Building the Product with the GWT Fork

The ``configuration/buildAndUpdateProduct.sh`` script combines all build steps necessary to build our product, with or without test execution, with or without the Gradle build for our mobile Android apps, and at its core with a Maven build that together with the Maven Tycho plugin builds our OSGi product as its main outcome. The Maven build uses the ``gwt-maven-plugin`` for all GWT compilations. Furthermore, we provide GWT to the OSGi environment through three wrapper bundles: ``com.google.gwt.[dev|servlet|user]``, each containing one or more of the GWT JARs found in the GWT distribution (``gwt-dev.jar``, ``gwt-servlet.jar``, ``gwt-servlet-deps.jar``, and ``gwt-user.jar``). Their OSGI manifests (``META-INF/MANIFEST.MF``) contain the GWT version number as their bundle version, as well as the list of packages to export. As explained above, you can install your forked GWT version into your workspace. The ``configuration/install-gwt`` script helps you with the production and distribution of the GWT artifacts into your local workspace, and it installs the necessary artifacts into your local Maven repository, enabling a local Maven-based product build.

The ``configuration/install-gwt`` script may be included in the ``configuration/buildAndUpdateProduct.sh`` script, especially when a forked GWT version is to be used on that branch for building the product in our central CI.

The Maven build will then use the forked ``gwt-maven-plugin`` which has been built with the forked GWT version. It builds the wrapper bundles that have been updated with the forked GWT's JARs and probably a changed version number in the bundle manifest. As a result you will obtain a product that is consistent with the forked GWT version.

## Running with the GWT Fork

Should the GWT fork require additional parameters, such as, e.g., the specification of a ``-Dgwt.rpc.version`` system property when launching a VM, then these parameters must be specified in three kinds of locations:

- in the ``parameters.common`` property in the ``java/pom.xml`` file
- in all launch configurations that you use to launch a server environment locally (e.g., ``SailingServer (No Proxy)``)
- in the ``COMMON_JAVA_ARGS`` parameter in the ``java/target/start`` script

When running locally, from your Eclipse environment, again make sure to have the correct GWT version selected in your Preferences / GWT / GWT Settings tab. If you're launching a server environment using the ``java/target/start`` script as deployed in a typical server instance directory, you don't need to worry because the ``start`` script is a copy of the ``java/target/start`` script you modified in the step above.

## The ``gwt-2.11.0.zip`` Release Candidate for bug5077

There is a pull request pending that contributes an improved RPC serialization protocol implementation which allows the server to stream the response payload instead of producing it entirely and multiply in memory before sending it out. See [https://github.com/gwtproject/gwt/pull/9779](https://github.com/gwtproject/gwt/pull/9779). This pull request allows us to build a special proxy for large payloads constructed from server-side data where the proxy has its own custom field serializer which constructs all data required for DTO construction from the server-side objects on the fly and writes them to the stream as primitive values (int, double, ...) instead of objects. This has vast benefits for memory consumption on the server side. See also the discussion on [bug5077](https://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=5077) and at the [GWT issue 7987](https://github.com/gwtproject/gwt/issues/7987).

The Github fork [https://github.com/axeluhl/gwt/tree/issue-7987-compatibility](https://github.com/axeluhl/gwt/tree/issue-7987-compatibility) holds the branch to be pulled. It is the basis for the ``configuration/install-gwt`` script to build the GWT artifacts. With branch ``bug5077`` the ``configuration/buildAndUpdateProduct.sh`` script is now using the ``configuration/install-gwt`` script. To work with this branch (and with the ``master`` branch going forward, once ``bug5077`` has been merged to ``master``) developers have to obtain the GWT installation ZIP for the fork. We make the latest copy available [here](https://static.sapsailing.com/gwt-2.11.0.zip).

With branch ``bug5077`` the [on-boarding document](/wiki/howto/onboarding) has been adjusted accordingly regarding where to get a GWT copy from.