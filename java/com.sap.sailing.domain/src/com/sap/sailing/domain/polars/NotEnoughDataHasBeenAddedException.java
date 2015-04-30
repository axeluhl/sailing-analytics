package com.sap.sailing.domain.polars;


public class NotEnoughDataHasBeenAddedException extends Exception {

    private static final long serialVersionUID = 4134116458606258315L;

    public NotEnoughDataHasBeenAddedException() {
        super("No data has been added to the Processor yet. At least to value-pairs have to be added.");
    }

    public NotEnoughDataHasBeenAddedException(String string) {
        super(string);
    }

    public NotEnoughDataHasBeenAddedException(String string, Throwable cause) {
        super(string, cause);
    }

}
