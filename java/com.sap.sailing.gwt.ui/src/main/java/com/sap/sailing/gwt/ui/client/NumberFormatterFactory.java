package com.sap.sailing.gwt.ui.client;

import com.google.gwt.i18n.client.NumberFormat;

public class NumberFormatterFactory {
    public static NumberFormat getDecimalFormat(int decimals) {
        StringBuilder patternBuilder = new StringBuilder("0");
        if (decimals > 0) {
            patternBuilder.append('.');
        }
        for (int i = 0; i < decimals; i++) {
            patternBuilder.append('0');
        }
        return NumberFormat.getFormat(patternBuilder.toString());
    }
    
    public static NumberFormat getDecimalFormat(int integerDigits, int decimals) {
        StringBuilder patternBuilder = new StringBuilder();
        for (int i=0; i<integerDigits; i++) {
            patternBuilder.append('0');
        }
        if (decimals > 0) {
            patternBuilder.append('.');
        }
        for (int i = 0; i < decimals; i++) {
            patternBuilder.append('0');
        }
        return NumberFormat.getFormat(patternBuilder.toString());
    }
}
