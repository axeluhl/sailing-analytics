# Profiling

Use the SAP JVM 7 available at http://sapjvm.wdf.sap.corp:1080 and its Eclipse profiler plug-in available from the same location. The update site to add to your Eclipse is http://sapjvm.wdf.sap.corp:1080/profiling. After unpacking the VM, make it known by your Eclipse installation and use it for the launch configuration that you want to profile.

## Profiling GWT Client Code

Using GWT's DevMode, you can also profile GWT Java code running in the client. You have to keep in mind that many things work differently when in DevMode and that significant overhead for the Browser-to-JavaVM communication is generated. However, your core algorithms that require attention performance-wise will often still stick out, assuming some clever filtering in the Eclipse profiler plug-in which is easily possible.

For GWT profiling please note that there is no immediate support to launch an SAP JVM in profiling mode for a GWT launch config. Instead, use the `Sailing GWT Remote Profiling (Run-Mode only)` launch configuration in Run mode. It will pause at start-up and wait for the profiler to get connected. Then use the profiling launch configuration `Profile SailingGWT` to get started.