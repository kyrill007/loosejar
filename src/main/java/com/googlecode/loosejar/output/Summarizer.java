package com.googlecode.loosejar.output;

import java.util.List;
import java.util.Map;

public interface Summarizer {

    String summarize(Map<ClassLoader, List<String>> classLoaderToClassListMap);
}
