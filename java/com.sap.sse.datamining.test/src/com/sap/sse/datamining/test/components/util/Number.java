package com.sap.sse.datamining.test.components.util;

import com.sap.sse.datamining.annotations.Dimension;

/*
 * DON'T CHANGE THE METHOD/CLASS NAMES!
 * The tests will fail, because they are reflected via constant strings.
 */

public class Number {
    
    private int value;

    public Number(int value) {
        this.value = value;
    }
    
    @Dimension("length")
    public int getLength() {
        return String.valueOf(value).length();
    }
    
    @Dimension("crossSum")
    public int getCrossSum() {
        int crossSum = 0;
        while (value != 0) {
              crossSum += value % 10;
              value /= 10;
        }
        return crossSum;
    }

}
