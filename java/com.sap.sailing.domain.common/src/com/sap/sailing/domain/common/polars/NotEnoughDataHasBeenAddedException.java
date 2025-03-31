package com.sap.sailing.domain.common.polars;

import java.io.Serializable;

public class NotEnoughDataHasBeenAddedException extends Exception implements Serializable {
    private static final long serialVersionUID = 9191379705907500676L;

    public NotEnoughDataHasBeenAddedException() {
        super("No data has been added to the Processor yet. At least two value-pairs have to be added.");
    }

    public NotEnoughDataHasBeenAddedException(String string) {
        super(string);
    }

    public NotEnoughDataHasBeenAddedException(String string, Throwable cause) {
        super(string, cause);
    }

}
