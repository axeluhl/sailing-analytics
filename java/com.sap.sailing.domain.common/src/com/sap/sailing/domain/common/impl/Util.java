package com.sap.sailing.domain.common.impl;

import com.sap.sse.common.Named;

public class Util extends com.sap.sse.common.Util {
    public static String join(String separator, Iterable<? extends Named> nameds) {
        return join(separator, toArray(nameds, new Named[size(nameds)]));
    }

    public static String join(String separator, Named... nameds) {
        String[] strings = new String[nameds.length];
        for (int i=0; i<nameds.length; i++) {
            strings[i] = nameds[i].getName();
        }
        return join(separator, strings);
    }
    
}
