# Working with GWT Locally

We're using GWT in a slightly non-standard way, combining it with the merits of OSGi and using separate GWT modules that are shared between server and client (such as com.sap.sailing.domain.common). Particularly the combination of GWT with OSGi comes with a few special arrangements and things that are noteworthy.

## javax.servlet Version

GWT comes with a built-in copy of the javax.servlet packages. However, in our environment, the servlet engine is provided by the Jetty bundles whose javax.servlet versions don't necessarily match with the one provided by the GWT version we use. To make the build use the javax.servlet version provided at runtime by Jetty, using the GWT classpath container that usually is added automatically to all GWT projects is detrimental to a proper build. That's why we have removed it in particular from the com.sap.sailing.gwt.ui project.

Since the OSGi manifest dependencies still require javax.servlet, the Eclipse and Maven builds now use the correct Jetty-provided version of javax.servlet. However, not having the GWT classpath container on a GWT project's classpath unfortunately makes it impossible to use the GWT Eclipse plugin's neat GWT Compile feature.

As a workaround, use the buildAndUpdateProduct.sh script found in the top-level configuration/ directory in git. Together with the -t and -c options (no tests, no cleaning), the compilation speed may be found acceptable. If you really urgently need to compile GWT locally on a regular basis and would like to speed up compilation, please consider temporarily editing java/com.sap.sailing.gwt.ui/pom.xml to reduce the number of modules and permutations to be compiled.