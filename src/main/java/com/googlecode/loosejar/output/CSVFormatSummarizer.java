package com.googlecode.loosejar.output;

import java.util.List;
import java.util.Map;

import com.googlecode.loosejar.ClassLoaderAnalyzer;
import com.googlecode.loosejar.JarArchive;

public class CSVFormatSummarizer implements Summarizer {

    private StringBuilder builder = null;

    public CSVFormatSummarizer() {
        super();
    }

    private void writeHeader() {
        builder.append("\"ClassLoader Name\",");
        builder.append("\"Jar\",");
        builder.append("\"Utilization\",");
        builder.append("\"Loaded Classes\",");
        builder.append("\"Total Classes\"");
        builder.append("\n");
    }

    private void writeSummaryForJar(ClassLoader classLoader, JarArchive jar) {
        builder.append((String.format("\"%s\",\"%s\",\"%.2f%%\",\"%d\",\"%d\"\n", classLoader.getClass().getName(),
                jar.getJar(), jar.getUsagePercentage(), jar.getNamesOfLoadedClasses().size(),
                jar.getAllClassNames().size())));

    }

    public String summarize(Map<ClassLoader, List<String>> classLoaderToClassListMap) {
        builder = new StringBuilder("");
        writeHeader();
        for (ClassLoader ucl : classLoaderToClassListMap.keySet()) {
            ClassLoaderAnalyzer classLoaderAnalyzer = new ClassLoaderAnalyzer(ucl, classLoaderToClassListMap.get(ucl));
            classLoaderAnalyzer.analyze();
            List<JarArchive> jarList = classLoaderAnalyzer.getJars();
            writeSummaryForClassloader(ucl, jarList);
        }

        return builder.toString();

    }

    private void writeSummaryForClassloader(ClassLoader classLoader, List<JarArchive> jarList) {
        for (JarArchive jarArchive : jarList) {
            writeSummaryForJar(classLoader, jarArchive);
        }

    }
}
