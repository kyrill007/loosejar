package com.googlecode.loosejar.output;

import java.util.List;
import java.util.Map;

import com.googlecode.loosejar.ClassLoaderAnalyzer;
import com.googlecode.loosejar.JarArchive;

public class VerbalSummarizer implements Summarizer {

    public String summarize(Map<ClassLoader, List<String>> classLoaderToClassListMap) {
        StringBuilder builder = new StringBuilder();
        //noinspection Duplicates
        for (ClassLoader ucl : classLoaderToClassListMap.keySet()) {
            ClassLoaderAnalyzer classLoaderAnalyzer = new ClassLoaderAnalyzer(ucl, classLoaderToClassListMap.get(ucl));
            classLoaderAnalyzer.analyze();
            List<JarArchive> jarList = classLoaderAnalyzer.getJars();
            writeSummaryForClassloader(ucl, jarList, builder);
        }
        return builder.toString();
    }

    private void writeSummaryForClassloader(ClassLoader classLoader, List<JarArchive> jars, StringBuilder builder) {
        builder.append("Summary for [" + classLoader.getClass().getName() + "] classloader:\n\n");
        //noinspection Duplicates
        for (JarArchive jar : jars) {
            builder.append("    ");
            builder.append("Jar: " + jar.getJar() + '\n');
            builder.append("    ");
            builder.append(
                    String.format(
                            "Utilization: %.2f%% - loaded %d of %d classes.\n\n",
                            jar.getUsagePercentage(),
                            jar.getNamesOfLoadedClasses().size(),
                            jar.getAllClassNames().size()
                    )
            );
        }
    }
}
