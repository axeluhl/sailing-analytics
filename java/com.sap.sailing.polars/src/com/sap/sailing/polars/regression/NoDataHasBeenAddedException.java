package com.sap.sailing.polars.regression;

public class NoDataHasBeenAddedException extends Exception {

    private static final long serialVersionUID = 4134116458606258315L;

    public NoDataHasBeenAddedException() {
        super("No data has been added to the Processor yet.");
    }

}
