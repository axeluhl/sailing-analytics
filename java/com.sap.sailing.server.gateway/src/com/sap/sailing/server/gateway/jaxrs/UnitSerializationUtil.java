package com.sap.sailing.server.gateway.jaxrs;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class UnitSerializationUtil {
    public static final DecimalFormat distanceDecimalFormatter = new DecimalFormat("#.##");
    public static final DecimalFormat knotsDecimalFormatter = new DecimalFormat("#.##");
    public static final DecimalFormat bearingDecimalFormatter = new DecimalFormat("#.#");
    public static final DecimalFormat speedDecimalFormatter = new DecimalFormat("#.#");
    public static final DecimalFormat latLngDecimalFormatter = new DecimalFormat("#.######");

    static {
        DecimalFormatSymbols symbol = DecimalFormatSymbols.getInstance();
        symbol.setDecimalSeparator('.');
        distanceDecimalFormatter.setDecimalFormatSymbols(symbol);
        knotsDecimalFormatter.setDecimalFormatSymbols(symbol);
        bearingDecimalFormatter.setDecimalFormatSymbols(symbol);
        speedDecimalFormatter.setDecimalFormatSymbols(symbol);
        latLngDecimalFormatter.setDecimalFormatSymbols(symbol);
    }
}
