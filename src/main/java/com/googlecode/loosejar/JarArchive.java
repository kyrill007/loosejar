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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This class represents a jar file, all classes that it contains, and a subset of those that have
 * been loaded into the JVM.
 *
 * @author Kyrill Alyoshin
 */
public class JarArchive {
    private final File jar;
    private final Set<String> allClassNames = new HashSet<String>();

    private Set<String> namesOfLoadedClasses;

    /**
     * Create an instance and convert internal jar class entries into valid class names.
     */
    public JarArchive(File jar) {
        this.jar = jar;

        for (String entry : getEntries(jar)) {
            if (!entry.endsWith(".class")) continue;

            //convert 'name/of/package/someclass.class' into 'name.of.package.someclass'
            allClassNames.add(entry.substring(0, entry.length() - ".class".length()).replace('/', '.'));
        }
    }

    /**
     * Return a file representing this jar.
     */
    public File getJar() {
        return jar;
    }

    /**
     * Return an <em>unmodifiable</em> set of class names present in this jar file.
     */
    public Set<String> getAllClassNames() {
        return Collections.unmodifiableSet(allClassNames);
    }

    /**
     * Return names of classes loaded from this jar.
     */
    public Set<String> getNamesOfLoadedClasses() {
        return Collections.unmodifiableSet(namesOfLoadedClasses);
    }

    /**
     * Set names of classes loaded from this jar.
     */
    public void setNamesOfLoadedClasses(Set<String> namesOfLoadedClasses) {
        this.namesOfLoadedClasses = namesOfLoadedClasses;
    }

    /**
     * Return the percentage of classes loaded into the JVM in comparison to all available in this jar file.
     */
    public double getUsagePercentage() {
        if (allClassNames.isEmpty()) return 0;

        return ((double) namesOfLoadedClasses.size() / (double) allClassNames.size()) * 100;
    }

    /**
     * Check whether there exists at least one class loaded from this jar.
     */
    public boolean isUsed() {
        return !namesOfLoadedClasses.isEmpty();
    }

    private List<String> getEntries(File archive) {
        Enumeration<JarEntry> entries;
        try {
            entries = new JarFile(archive).entries();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read entries from a jar archive [" + archive + "]: " + e);
        }

        List<String> names = new ArrayList<String>();
        while (entries.hasMoreElements()) {
            names.add(entries.nextElement().getName());
        }
        return names;
    }


}
