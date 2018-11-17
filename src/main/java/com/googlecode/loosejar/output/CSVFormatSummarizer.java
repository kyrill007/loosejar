package com.googlecode.loosejar.output;

import java.util.List;
import java.util.Map;

import com.googlecode.loosejar.ClassLoaderAnalyzer;
import com.googlecode.loosejar.JarArchive;

public class CSVFormatSummarizer implements Summarizer {

    public String summarize(Map<ClassLoader, List<String>> classLoaderToClassListMap) {
        StringBuilder builder = new StringBuilder();

        writeHeader(builder);

        //noinspection Duplicates
        for (ClassLoader ucl : classLoaderToClassListMap.keySet()) {
            ClassLoaderAnalyzer classLoaderAnalyzer = new ClassLoaderAnalyzer(ucl, classLoaderToClassListMap.get(ucl));
            classLoaderAnalyzer.analyze();
            List<JarArchive> jarList = classLoaderAnalyzer.getJars();
            writeSummaryForClassloader(ucl, jarList, builder);
        }

        return builder.toString();
    }

    private void writeHeader(StringBuilder builder) {
        builder.append("\"ClassLoader Name\",");
        builder.append("\"Jar\",");
        builder.append("\"Utilization\",");
        builder.append("\"Loaded Classes\",");
        builder.append("\"Total Classes\"");
        builder.append("\n");
    }

    private void writeSummaryForJar(ClassLoader classLoader, JarArchive jar, StringBuilder builder) {
        builder.append((String.format("\"%s\",\"%s\",\"%.2f%%\",\"%d\",\"%d\"\n", classLoader.getClass().getName(),
                jar.getJar(), jar.getUsagePercentage(), jar.getNamesOfLoadedClasses().size(),
                jar.getAllClassNames().size())));
    }

    private void writeSummaryForClassloader(ClassLoader classLoader, List<JarArchive> jarList, StringBuilder builder) {
        for (JarArchive jarArchive : jarList) {
            writeSummaryForJar(classLoader, jarArchive, builder);
        }
    }
}
