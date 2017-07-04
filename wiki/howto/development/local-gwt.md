# Working with GWT Locally

We're using GWT in a slightly non-standard way, combining it with the merits of OSGi and using separate GWT modules that are shared between server and client (such as com.sap.sailing.domain.common). Particularly the combination of GWT with OSGi comes with a few special arrangements and things that are noteworthy.

## Some Background (javax.servlet Version)

GWT comes with a built-in copy of the javax.servlet packages. However, in our environment, the servlet engine is provided by the Jetty bundles whose javax.servlet versions don't necessarily match with the one provided by the GWT version we use. To make the build use the javax.servlet version provided at runtime by Jetty, using the GWT classpath container that usually is added automatically to all GWT projects is detrimental to a proper build. That's why we have removed it in particular from the com.sap.sailing.gwt.ui project.

Since the OSGi manifest dependencies still require javax.servlet, the Eclipse and Maven builds now use the correct Jetty-provided version of javax.servlet. However, not having the GWT classpath container on a GWT project's classpath unfortunately makes it impossible to use the GWT Eclipse plugin's neat GWT Compile feature.

## Local GWT Compile

As a workaround, use the buildAndUpdateProduct.sh script found in the top-level configuration/ directory in git. Together with the -t and -c options (no tests, no cleaning), the compilation speed may be found acceptable. If you really urgently need to compile GWT locally on a regular basis and would like to speed up compilation, please consider temporarily editing java/com.sap.sailing.gwt.ui/pom.xml to reduce the number of modules and permutations to be compiled.

Add restrictions with the following two tags to reduce the number of permutations so that gwt is compiled only in your language and browser:

<set-property name="locale" value="en" />

`<set-property name="user.agent" value="gecko1_8" />`

`<set-property name="locale" value="en" />`

Those tags must be put into the *.gwt.xml files e.g. "AdminConsole.gwt.xml".

## Debugging GWT

One of the great strengths of GWT is the use of Eclipse as a Java source-level debugging environment. To enjoy this feature, launch the SailingServer launch config appropriate for your environment (Proxy / No Proxy), then launch the SailingGWT launch configuration in debug mode. After a while it will show a "Development Mode" view that shows all entry points that have been initialized. Double-click on the one you want to debug, and your default browser (hopefully FireFox, because with other browsers the GWT debug plugin tends to be not very stable or not even present) will open.

You can set breakpoints in your GWT Java code and inspect values for suspended threads as usual for any Java development.

Note the reduced performance which is largely due to the way the Java VM and the browser plug-in communicate. In particular, many fine-grained changes to the DOM can be quite costly. Therefore, if you're only interested in server-side debugging, consider [compiling](#Local-GWT-Compile) the GWT code for better performance.

## Curious Errors

There's a randomly occuring error, since the update to GWT 2.7, where an entry point fails to load. The Development Mode view of Eclipse shows the error  
<pre>
Your source appears not to live underneath a subpackage called 'client'; no problem, but you'll need to use the &lt;source&gt; directive in your module to make it accessible
</pre>
The problem can be "solved" by retrying until it works.

## i18n / permutations


GWT generates one permutation per configured locale, in short:

 * locale configuration is kept in the module descriptors
 * localized text is stored in utf-8 property files
 
The [online GWT i18n guide](http://www.gwtproject.org/doc/latest/DevGuideI18n.html) provides further/ detail information of how i18n is solved in GWT
 
The build script uses the "-b" option to generate just one permutation. 
This switch simply replaces all references to "AllPermutations" to "SinglePermutation" gwt module descriptors. 
The "AllPermutations" gwt modules contain all locales that should be generated, while the "SinglePermutation" modules restrict the compilation to the default locale (en) and to the gecko browser.
 
See:

  * SailingLocalesAllPermutations.gwt.xml
  * SailingLocalesSinglePermutation.gwt.xml
  * com.sap.sailing.gwt.ui.client.StringMessages
  * com.sap.sailing.gwt.ui/src/main/java/com/sap/sailing/gwt/ui/client/StringMessages.properties
 
 