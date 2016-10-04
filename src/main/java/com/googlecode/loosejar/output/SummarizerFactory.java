package com.googlecode.loosejar.output;

public class SummarizerFactory {

    public Summarizer getSummarizer() {

        String formatString = System.getProperty("loosejar.format");
        Format format = (formatString == null || formatString.equals("")) ? Format.VERBAL
                : Format.fromString(formatString);

        Summarizer writer = null;

        switch (format) {
            case CSV:
                writer = new CSVFormatSummarizer();
                break;
            default:
                writer = new VerbalSummarizer();
        }

        return writer;
    }

}
