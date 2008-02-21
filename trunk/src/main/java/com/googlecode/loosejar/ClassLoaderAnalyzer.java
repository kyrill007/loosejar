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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import static com.googlecode.loosejar.Constants.*;
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
    private static final String MANIFEST_URL_PREFIX = "jar:";
    private static final String MANIFEST_URL_SUFFIX = "!/META-INF/MANIFEST.MF";

    private final URLClassLoader classLoader;
    private final List<String> classLoaderClasses;

    private final List<JarArchive> jars = new ArrayList<JarArchive>();

    /**
     * Create an instance of the class and determine all the jars on the supplied classloader's classpath.
     *
     * @param classLoader        the classloader to be analyzed
     * @param classLoaderClasses the classes that this classloaded has loaded
     */
    public ClassLoaderAnalyzer(URLClassLoader classLoader, List<String> classLoaderClasses) {
        this.classLoader = classLoader;
        this.classLoaderClasses = classLoaderClasses;
        this.jars.addAll(findAllJars());
    }

    private List<JarArchive> findAllJars() {
        List<JarArchive> list = new ArrayList<JarArchive>();

        Enumeration<URL> urls;
        try {
            // This will return a transitive closure of all jars on the classpath
            // in the form of
            // jar:file:/foo/bar/baz.jar!/META-INF/MANIFEST.MF
            urls = classLoader.findResources("META-INF/MANIFEST.MF");
        } catch (IOException e) {
            //presumably it should never happen
            throw new RuntimeException(e);
        }

        while (urls.hasMoreElements()) {
            String rawUrl = urls.nextElement().toString();

            //convert into a normal URI
            int start = MANIFEST_URL_PREFIX.length();
            int end = rawUrl.length() - MANIFEST_URL_SUFFIX.length();
            String uriStr = rawUrl.substring(start, end);

            //we don't want to examine JDK jars;
            //ignore own loosejar.jar as well
            if (uriStr.contains(JAVA_HOME.toString()) || uriStr.contains(PROJECT_NAME)) {
                continue;
            }

            URI uri = null;
            try {
                uri = new URI(uriStr);
            } catch (URISyntaxException e) {
                new RuntimeException(e);
            }

            File jar = new File(uri);
            // just real jars are needed,
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
        StringBuilder sb = new StringBuilder();

        sb.append("Summary for [" + classLoader.getClass().getName() + "] classloader:\n\n");
        for (JarArchive jar : jars) {
            sb.append("    ");
            sb.append("Jar: " + jar.getJar() + '\n');
            sb.append("    ");
            sb.append(String.format("Utilization: %.2f%% - loaded %d of %d classes.\n\n",
                    jar.getUsagePercentage(), jar.getNamesOfLoadedClasses().size(), jar.getAllClassNames().size()));
        }

        if (jars.isEmpty()) {
            sb.append("    ");
            sb.append("No third-party jars detected on this classloader's classpath.\n\n");
        }

        return sb.toString();
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
        } catch (MalformedURLException e) {
            //this shouldn't happen; the value of java.home system property should be always parseable.
            throw new RuntimeException(e);
        }
    }
}

