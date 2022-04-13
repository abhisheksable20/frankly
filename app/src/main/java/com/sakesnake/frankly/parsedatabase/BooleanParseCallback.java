package com.sakesnake.frankly.parsedatabase;

import com.parse.ParseException;

public interface BooleanParseCallback {
    void isTrueFromServer(boolean isTrue, ParseException parseException);
}
