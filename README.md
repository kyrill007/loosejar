Description
===========
**loosejar** is a simple *Java Agent* that can be used to discover unnecessary jars lying on application classpath. It performs per classloader JVM heap analysis and displays its results. loosejar can be safely used during development, QA, UAT or even in production as it doesn't modify the state of the JVM at all and adds no overhead.

The usage is very simple:

1. Start your application or application server with `-javaagent:loosejar.jar` flag (loosejar.jar should obviously point to the correct path of the actual jar)
2. Exercise your application to make sure that the classes get loaded into the JVM.
3. Get loosejar analysis results via JMX console (open jconsole and run `com.googlecode.loosejar.LooseJarMBean#summary()` in `MBeans` folder) or on application shutdown (via regular console log).

loosejar can only be used on Java 1.5 or higher JVMs.

Releases
--------
loosejar **1.0.1** has been released!

Mentions
--------
[Eamonn McManus's Blog](http://weblogs.java.net/blog/emcmanus/archive/2008/02/do_i_really_nee.html)
