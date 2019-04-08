# JMX Monitoring

Java offers Java Management Extensions (JMX) which is built into the JDK since Java 6. We can use this to monitor the state of a live server VM without the need to attach a debugger or profiler and can thus gain at least some high-level insights into the VM's health status.

Our first JMX MXBean is `com.sap.sailing.server.RacingEventServiceMXBean` for which an instance is created and registered by the `com.sap.sailing.server.impl.Activator` and which reads properties from the `RacingEventService` instance. This can also be used as an example for further MXBeans that make other aspects of our server manageable and monitorable in the console.

In order to gain access to the VM using JMX it needs to be started with a few system properties set:
<pre>
  -Dcom.sun.management.jmxremote
</pre>

You can then launch your local `jconsole` tool from the JDK to connect to the remote VM. Ideally, you launch a VNC server on the machine running the Java VM that you want to monitor and execute `jconsole` in a terminal running in that VNC server. This avoids firewall hassle which can be pretty difficult to handle when trying to monitor a JMX-enabled VM remotely.

![JConsole Screenshot](/wiki/images/jconsole.png)
<center>Screenshot of a VNC viewer showing the running JConsole client.</center>