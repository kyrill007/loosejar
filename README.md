Description
===========
**loosejar** is a simple *Java Agent* that can be used to discover unnecessary jars lying on application classpath. It performs per classloader JVM heap analysis and displays its results. The results can be extracted in the form of comma separated values (csv) or the default verbal mechanism. loosejar can be safely used during development, QA, UAT or even in production as it doesn't modify the state of the JVM at all and adds no overhead. loosejar can also extract the results into a file.

The usage is very simple:

1. Start your application or application server with `-javaagent:loosejar.jar` flag (loosejar.jar should obviously point to the correct path of the actual jar) with or without the configuration properties.S
2. Exercise your application to make sure that the classes get loaded into the JVM.
3. Get loosejar analysis results via JMX console (open jconsole and run `com.googlecode.loosejar.LooseJarMBean#summary()` in `MBeans` folder) or on application shutdown.

loosejar can only be used on Java 1.5 or higher JVMs.

Configuration
-------------
loosejar can be configured using java system properties. Following are the configurations available in loosejar

1. `loosejar.format` - This is a non mandatory property which can be used to specify the output format, supports values csv or verbal for now. This configuration also affects the output format of results extracted via JMX console.
2. `loosejar.outputFile` - This is a non mandatory property which can be used to extracts the results to the file. If this option is not specified loosejar prints results on to console (System.out). This configuration is only applied on application shutdown.

Releases
--------
loosejar **1.0.1** has been released!

Mentions
--------
[Eamonn McManus's Blog](http://weblogs.java.net/blog/emcmanus/archive/2008/02/do_i_really_nee.html)
