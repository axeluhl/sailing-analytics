package com.sap.sailing.domain.common.trackfiles;

public enum TrackFilesFormat {
    Gpx10("gpx"), Gpx11("gpx"), Kml20("kml"), Kml21("kml"), Kml22("kml"), Kmz20("kmz"), Kmz21("kmz"), Kmz22("kmz"),
    // GarminMapSource5("gdb"),
    // GarminMapSource6("gdb"),
    MagicMapsIkt("ikt"),
    // NokiaLandmarkExchange("lmx"),
    Ovl("ovl"), OziExplorerTrack("plt"), Tcx1("tcx"), Tcx2("tcx"), CSV("csv");

    public final String suffix;

    private TrackFilesFormat(String suffix) {
        this.suffix = suffix;
    }
}
