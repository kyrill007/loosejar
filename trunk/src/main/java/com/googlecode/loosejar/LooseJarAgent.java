/*
 *  Copyright 2001-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.googlecode.loosejar;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;

import static com.googlecode.loosejar.Constants.*;
import static com.googlecode.loosejar.Logger.*;

/**
 * This is the <em>Java Agent</em> of the project. It represents an entry point into the application
 * to be specified at the JVM startup. The {@link #premain} method registers the analyzer
 * ({@link JVMAnalyzer} to be run on application shutdown and as a JMX service.
 *
 * @author Kyrill Alyoshin
 */
public class LooseJarAgent {
    /**
     * Registers {@link JVMAnalyzer} to run at application shutdown and as a JMX service.
     */
    public static void premain(String args, Instrumentation instrumentation) {
        registerOnShutdown(instrumentation);
        registerWithJmx(instrumentation);
    }

    private static void registerOnShutdown(Instrumentation instrumentation) {
        Runtime.getRuntime().addShutdownHook(new Thread(new JVMAnalyzer(instrumentation)));
        log(PROJECT_NAME + " analysis is registered to run on JVM shutdown.");
    }

    private static void registerWithJmx(Instrumentation instrumentation) {
        log("Registering " + PROJECT_NAME + " as a JMX service...");
        String jmxName = LooseJarMBean.class.getPackage().getName() + ":type=" + LooseJarMBean.class.getSimpleName();

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        boolean success = true;

        ObjectName name = null;
        try {
            name = new ObjectName(jmxName);
        }
        catch (MalformedObjectNameException e) {
            success = false;
            log("Failed to register " + PROJECT_NAME + " with JMX, because the object name is malformed: " + e);
        }

        if (name != null) {
            try {
                mbs.registerMBean(new LooseJar(instrumentation), name);
            }
            catch (InstanceAlreadyExistsException e) {
                success = false;
                log("Failed to register " + PROJECT_NAME + " with JMX, " +
                        "because the instance is already registered: " + e);
            }
            catch (MBeanRegistrationException e) {
                success = false;
                log("Failed to register " + PROJECT_NAME + " with JMX due to an unknown exception: " + e);
            }
            catch (NotCompliantMBeanException e) {
                success = false;
                log("Failed to register " + PROJECT_NAME + " with JMX due to an unknown exception: " + e);
            }
        }

        if (success) {
            log("Registered " + PROJECT_NAME + " as a JMX service: [" + jmxName + "]\n");
        }
        else {
            log("JMX Registration failed!\n");
        }
    }

}



