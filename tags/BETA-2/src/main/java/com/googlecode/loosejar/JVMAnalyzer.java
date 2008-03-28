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

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.googlecode.loosejar.Logger.*;


/**
 * This class represents the logical point of entry into the application. It analyzes the JVM
 * state and creates a map of classloaders to their loaded classes. The {@link #displayResults} method
 * delegates further processing of individual classloader data to the {@link ClassLoaderAnalyzer} class.
 *
 * @author Kyrill Alyoshin
 */
public class JVMAnalyzer implements Runnable {
    private final Instrumentation instrumentation;

    public JVMAnalyzer(Instrumentation instr) {
        this.instrumentation = instr;
    }

    /**
     * Simply invokes {@link #displayResults()}.
     */
    public void run() {
        displayResults();
    }

    /**
     * Performs <em>all</em> application logic returning the results of the analysis.
     */
    public String displayResults() {
        Map<ClassLoader, List<String>> classLoaderMap = createClassLoaderMap();

        StringBuilder sb = new StringBuilder();
        for (ClassLoader ucl : classLoaderMap.keySet()) {
            ClassLoaderAnalyzer cli = new ClassLoaderAnalyzer(ucl, classLoaderMap.get(ucl));
            cli.analyze();
            sb.append(cli.summary());
        }

        String results = sb.toString();
        log(results);
        return results;
    }

    private Map<ClassLoader, List<String>> createClassLoaderMap() {
        Map<ClassLoader, List<String>> map = new HashMap<ClassLoader, List<String>>();

        Class<?>[] loadedClasses = instrumentation.getAllLoadedClasses();
        log(String.format("Found %d classes loaded in the JVM.", loadedClasses.length));

        for (Class<?> c : loadedClasses) {
            ClassLoader cl = c.getClassLoader();
            if (cl == null) continue;  //we don't need Bootstrap classloader if it is represented as null

            if (map.containsKey(cl)) {
                map.get(cl).add(c.getName());
            }
            else {
                List<String> classNames = new ArrayList<String>();
                classNames.add(c.getName());
                map.put(cl, classNames);
            }
        }

        log(String.format("Found %d various ClassLoader(s) inside the JVM.", map.size()));
        return map;
    }


}
