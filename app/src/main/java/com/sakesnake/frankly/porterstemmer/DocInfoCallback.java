package com.sakesnake.frankly.porterstemmer;

@FunctionalInterface
public interface DocInfoCallback {
    void getDocInfo(int totalWords, String rootWords);
}
