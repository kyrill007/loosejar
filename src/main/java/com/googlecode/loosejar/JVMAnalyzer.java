/*
 *  Copyright 2001-2016 The Apache Software Foundation
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

import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.googlecode.loosejar.output.Summarizer;
import com.googlecode.loosejar.output.SummarizerFactory;

import static com.googlecode.loosejar.Logger.*;

/**
 * This class represents the logical point of entry into the application. It
 * analyzes the JVM state and creates a map of classloaders to their loaded
 * classes. The {@link #displayResults} method delegates further processing of
 * individual classloader data to the {@link ClassLoaderAnalyzer} class.
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
     * Performs <em>all</em> application logic returning the results of the
     * analysis.
     */
    public void displayResults() {
        String outputFile = System.getProperty("loosejar.outputFile");
        String results = getResults();
        if (outputFile == null || outputFile.equals("")) {
            writeToConsole(results);
        } else {
            writeToFile(outputFile, results);
        }

    }

    private Map<ClassLoader, List<String>> createClassLoaderMap() {
        Map<ClassLoader, List<String>> map = new HashMap<ClassLoader, List<String>>();

        Class<?>[] loadedClasses = instrumentation.getAllLoadedClasses();
        log(String.format("Found %d classes loaded in the JVM.", loadedClasses.length));

        for (Class<?> c : loadedClasses) {
            ClassLoader cl = c.getClassLoader();
            if (cl == null) {
                continue; // we don't need Bootstrap classloader if it is
            }
            // represented as null

            if (map.containsKey(cl)) {
                map.get(cl).add(c.getName());
            } else {
                List<String> classNames = new ArrayList<String>();
                classNames.add(c.getName());
                map.put(cl, classNames);
            }
        }

        log(String.format("Found %d various ClassLoader(s) inside the JVM.", map.size()));
        return map;
    }

    public String getResults() {
        SummarizerFactory factory = new SummarizerFactory();
        Summarizer summarizer = factory.getSummarizer();
        return summarizer.summarize(createClassLoaderMap());
    }

    private void writeToConsole(String results) {
        System.out.println(results);
    }

    private void writeToFile(String outputFile, String results) {
        PrintStream fileStream = null;
        try {
            fileStream = new PrintStream(outputFile);
            fileStream.println(results);

        } catch (IOException ioe) {
            log(String.format("Exception creating outputFile - %s, writing to default output console", outputFile));
            writeToConsole(results);
        } finally {
            if (fileStream != null) {
                fileStream.close();
            }
        }
    }
}
