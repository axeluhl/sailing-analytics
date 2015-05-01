package com.sap.sse.datamining.shared.data;

public enum Month {
    
    January, February, March, April, May, June, Juli, August, September, October, November, December;

    /**
     * Private cache of all the constants.
     */
    private static final Month[] VALUES = Month.values();
    
    /**
     * @param month the month as <code>int</code> to represent, from 1 (January) to 12 (December)
     * @return the month, not null
     * @throws IllegalArgumentException if the given month isn't valid
     */
    public static Month fromInt(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid value for month: " + month);
        }
        return VALUES[month - 1];
    }
    
    /**
     * Gets the month {@code int} value.<br />
     * The values are numbered from 1 (January) to 12 (December).
     *
     * @return the month as <code>int</code>, from 1 (January) to 12 (December)
     */
    public int asInt() {
        return ordinal() + 1;
    }

}
