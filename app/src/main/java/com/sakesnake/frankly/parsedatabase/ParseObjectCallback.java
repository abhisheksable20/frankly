package com.sakesnake.frankly.parsedatabase;

import com.parse.ParseException;
import com.parse.ParseObject;

@FunctionalInterface
public interface ParseObjectCallback {
    void parseObjectCallback(ParseObject object, ParseException parseException);
}
