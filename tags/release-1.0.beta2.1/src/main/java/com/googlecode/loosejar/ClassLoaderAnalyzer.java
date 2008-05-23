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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import static com.googlecode.loosejar.Constants.*;
import static com.googlecode.loosejar.Logger.*;
import com.googlecode.loosejar.org.apache.commons.collections15.CollectionUtils;

/**
 * The purpose of this class is to:
 * <ul>
 * <li> determine all of the the jars on the classpath </li>
 * <li> discover which jars where exercised in respect to the classes loaded by the classloader </li>
 * <li> display the summary of the analysis </li>
 * </ul>
 *
 * @author Kyrill Alyoshin
 */
public class ClassLoaderAnalyzer {
    private static final URL JAVA_HOME = javaHome();
    private static final int MANIFEST_PREFIX_LENGTH = "jar:".length();
    private static final int MANIFEST_SUFFIX_LENGTH = "!/META-INF/MANIFEST.MF".length();

    private final ClassLoader classLoader;
    private final List<String> classLoaderClasses;

    private final List<JarArchive> jars = new ArrayList<JarArchive>();

    /**
     * Create an instance of the class and determine all the jars on the supplied classloader's classpath.
     *
     * @param classLoader        the classloader to be analyzed
     * @param classLoaderClasses the classes that this classloaded has loaded
     */
    public ClassLoaderAnalyzer(ClassLoader classLoader, List<String> classLoaderClasses) {
        this.classLoader = classLoader;
        this.classLoaderClasses = classLoaderClasses;
        this.jars.addAll(findAllJars());
    }

    private List<JarArchive> findAllJars() {
        List<JarArchive> list = new ArrayList<JarArchive>();

        Enumeration<URL> urls = findManifestResources();
        if (urls == null) return list;

        while (urls.hasMoreElements()) {
            String rawUrl = urls.nextElement().toString();

            if (!rawUrl.startsWith("jar:")) continue;

            //convert into a normal URI
            String uriStr = rawUrl.substring(MANIFEST_PREFIX_LENGTH, rawUrl.length() - MANIFEST_SUFFIX_LENGTH);

            //we don't want to examine JDK jars;
            //ignore own loosejar.jar as well
            if (uriStr.contains(JAVA_HOME.toString()) || uriStr.contains(PROJECT_NAME)) {
                continue;
            }

            uriStr = uriStr.replaceAll("\\s", "%20"); //escape spaces

            // this is a workaround for a common bug in some classloader implementations,
            // which often return URIs in the following format [file:c:/location/...]
            // the point is that 'file:' must be followed by '/' to be a valid URI.
            if (uriStr.startsWith("file:") && !uriStr.startsWith("file:/")) {
                uriStr = "file:/" + uriStr.substring("file:".length());
            }

            File jar;
            try {
                URI uri = new URI(uriStr);
                jar = new File(uri);
            }
            catch (Exception e) {
                log("IGNORED: [" + uriStr + "]. Bad URI syntax.");
                continue;
            }

            // just real jars are needed;
            // directories and incorrectly specified classpath entries are not needed.
            if (jar.isFile()) {
                list.add(new JarArchive(jar));
            }
        }
        return list;
    }

    /**
     * Return an <em>unmodifiable</em> list of jars on the classloader's classpath.
     */
    public List<JarArchive> getJars() {
        return Collections.unmodifiableList(jars);
    }

    /**
     * Perform main project analysis determining the relationship between available jars
     * and the classes loaded in the JVM.
     */
    public void analyze() {
        for (JarArchive jar : jars) {
            //find which classes loaded by this classloader came from a given jar.
            Collection<String> usedClasses = CollectionUtils.intersection(classLoaderClasses, jar.getAllClassNames());
            jar.setNamesOfLoadedClasses(new HashSet<String>(usedClasses));
        }
    }

    /**
     * Display the analysis summary.
     */
    public String summary() {
        if (jars.isEmpty()) return "";

        StringBuilder buf = new StringBuilder();
        buf.append("Summary for [" + classLoader.getClass().getName() + "] classloader:\n\n");
        for (JarArchive jar : jars) {
            buf.append("    ");
            buf.append("Jar: " + jar.getJar() + '\n');
            buf.append("    ");
            buf.append(String.format("Utilization: %.2f%% - loaded %d of %d classes.\n\n",
                    jar.getUsagePercentage(), jar.getNamesOfLoadedClasses().size(), jar.getAllClassNames().size()));
        }
        return buf.toString();
    }

    private static URL javaHome() {
        //return normalized URL
        File jHome = new File(System.getProperty("java.home"));
        String name = jHome.getName();

        // we're trying to get at the root of JDK or JRE here.
        // java.home system property whould typically be '$JAVA_HOME/jre',
        // but we want only $JAVA_HOME directory.
        if (name.equalsIgnoreCase("jre") || name.equalsIgnoreCase("lib")) {
            jHome = jHome.getParentFile();
        }

        try {
            return jHome.toURI().toURL();
        }
        catch (MalformedURLException e) {
            //this shouldn't happen; the value of java.home system property should be always parseable.
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Enumeration<URL> findManifestResources() {
        //invoke #findResource(String) method reflectively as it is protected in java.lang.ClassLoader
        try {
            Method method = findMethod(classLoader.getClass(), "findResources", new Class<?>[]{String.class});

            //attempt to disable security check for non-public methods.
            if (!Modifier.isPublic(method.getModifiers()) && !method.isAccessible()) {
                method.setAccessible(true);
            }

            // This will return a transitive closure of all jars on the classpath
            // in the form of
            // jar:file:/foo/bar/baz.jar!/META-INF/MANIFEST.MF
            return (Enumeration<URL>) method.invoke(classLoader, "META-INF/MANIFEST.MF");

        }
        catch (IllegalAccessException e) {
            log("Failed to invoke #findResources(String) method on classloader [" + classLoader + "].");
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
            log("Failed to invoke #findResources(String) method on classloader [" + classLoader + "].");
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(Class<?> clazz, String name, Class<?>[] paramTypes) {
        Class<?> type = clazz;
        while (!Object.class.equals(type) && type != null) {
            Method[] methods = type.getDeclaredMethods();
            for (Method method : methods) {
                if (name.equals(method.getName()) && Arrays.equals(paramTypes, method.getParameterTypes())) {
                    return method;
                }
            }
            type = type.getSuperclass();
        }
        return null;
    }
}

