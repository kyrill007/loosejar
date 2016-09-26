package com.googlecode.loosejar.output;

import java.util.List;
import java.util.Map;

import com.googlecode.loosejar.ClassLoaderAnalyzer;
import com.googlecode.loosejar.JarArchive;

public class VerbalSummarizer implements Summarizer {

    private StringBuilder builder = null;

    public VerbalSummarizer() {
        super();
    }

    protected void writeSummaryForClassloader(ClassLoader classLoader, List<JarArchive> jars) {
        builder.append("Summary for [" + classLoader.getClass().getName() + "] classloader:\n\n");
        for (JarArchive jar : jars) {
            builder.append("    ");
            builder.append("Jar: " + jar.getJar() + '\n');
            builder.append("    ");
            builder.append(String.format("Utilization: %.2f%% - loaded %d of %d classes.\n\n", jar.getUsagePercentage(),
                    jar.getNamesOfLoadedClasses().size(), jar.getAllClassNames().size()));
        }
    }

    public String summarize(Map<ClassLoader, List<String>> classLoaderToClassListMap) {
        builder = new StringBuilder("");
        for (ClassLoader ucl : classLoaderToClassListMap.keySet()) {
            ClassLoaderAnalyzer classLoaderAnalyzer = new ClassLoaderAnalyzer(ucl, classLoaderToClassListMap.get(ucl));
            classLoaderAnalyzer.analyze();
            List<JarArchive> jarList = classLoaderAnalyzer.getJars();
            writeSummaryForClassloader(ucl, jarList);
        }
        return builder.toString();
    }

}
