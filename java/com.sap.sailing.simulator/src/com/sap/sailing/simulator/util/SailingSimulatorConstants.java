package com.sap.sailing.simulator.util;

public interface SailingSimulatorConstants {
    public static final char ModeFreestyle = 'f'; // mode: 'f'reestyle definition of race course
    public static final char ModeMeasured = 'm';  // mode: 'm'easured race course & tracks
    public static final char ModeEvent = 'e';     // mode: 'e'vent entry overlay with regatta areas

    public static final char LegTypeUpwind = 'u';    // leg type: 'u'pwind
    public static final char LegTypeDownwind = 'd';  // leg type: 'd'ownwind
    
    public static final char EventDummy = '0';        // used in cases where race course is defined as URL parameter
    public static final char EventKielerWoche = 'k';        // event: 'k'ieler woche
    public static final char EventTravemuenderWoche = 't';  // event: 't'ravemünder woche
    public static final char EventX40Cardiff = 'c';  // event: 'c'ardiff extreme sailing series
}
