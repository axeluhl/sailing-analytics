package com.sap.sailing.domain.common;

import com.sap.sailing.domain.common.impl.MeterDistance;


public enum BoatClassMasterdata {
    _2_4M ("2.4 Meter", true, 4.11, 0.81, BoatHullType.MONOHULL, "2.4M", "2.4mR"),
    _5O5 ("5O5", true, 5.03, 1.88, BoatHullType.MONOHULL, "505", "5o5", "505er", "5o5er"),
    _12M ("12 Meter", true, 5.97, 1.43, BoatHullType.MONOHULL, "12M", "12mR", "12SQM"),
    _29ER ("29er", true, 4.45, 1.70, BoatHullType.MONOHULL),
    _29ERXX ("29erXX", true, 4.45, 1.77, BoatHullType.MONOHULL, "29XX"),
    _49ER ("49er", true, 4.88, 1.93, BoatHullType.MONOHULL),
    _49ERFX ("49erFX", true, 4.88, 1.93, BoatHullType.MONOHULL, "49FX"),
    _420 ("420", true, 4.20, 1.65, BoatHullType.MONOHULL, "420er", "420M", "420W"),
    _470 ("470", true, 4.70, 1.68, BoatHullType.MONOHULL, "470er", "470M", "470W"),
    A_CAT ("A-Catamaran", true, 5.49, 2.30, BoatHullType.CATAMARAN, "A-Cat", "ACat", "A-Class Catamaran"),
    ALBIN_EXPRESS ("Albin Express", true, 7.77, 2.50, BoatHullType.MONOHULL),
    B_ONE ("B/ONE", true, 7.80, 2.49, BoatHullType.MONOHULL, "B-ONE"),
    DRAGON_INT ("Dragon Int.", true, 8.89, 1.96, BoatHullType.MONOHULL, "Drachen", "Dragon"),
    EXTREME_40 ("Extreme 40", false, 12.2, 6.60, BoatHullType.CATAMARAN, "Extreme-40", "Extreme40"),
    EUROPE_INT ("Europe Int.", false, 3.35, 1.35, BoatHullType.MONOHULL, "Europe"),
    F_18 ("F 18", false, 6.85, 2.25, BoatHullType.CATAMARAN, "F-18"),
    FARR_30 ("Farr 30", false, 9.42, 3.08, BoatHullType.MONOHULL, "F30", "F-30", "Farr-30"),
    FINN ("Finn", false, 4.50, 1.51, BoatHullType.MONOHULL),
    FOLKBOAT ("Folkboat", false, 7.68, 2.20, BoatHullType.MONOHULL, "Folke", "Folkeboot"),
    F_16 ("Formula 16", false, 5.00, 2.50, BoatHullType.MONOHULL, "F16", "F-16"),
    HOBIE_16 ("Hobie 16", false, 5.05, 2.41, BoatHullType.CATAMARAN, "H16"),
    H_BOAT ("H-Boat", false, 8.28, 2.18, BoatHullType.MONOHULL, "HB"),
    HOBIE_TIGER ("Hobie Tiger", false, 5.51, 2.60, BoatHullType.CATAMARAN),
    HOBIE_WILD_CAT ("Hobie Wild Cat", false, 5.49, 2.59, BoatHullType.CATAMARAN, "Hobie Wild Cat F18"),
    J70 ("J/70", true, 6.93, 2.25, BoatHullType.MONOHULL, "J70", "J-70"),
    J80 ("J/80", true, 8.0, 2.51, BoatHullType.MONOHULL, "J80", "J-80"),
    J24 ("J/24", true, 7.32, 2.67, BoatHullType.MONOHULL, "J24", "J-24"),
    KITE ("Kite", true, 3.35, 1.52, BoatHullType.MONOHULL), //
    LASER_4_7 ("Laser 4.7", true, 4.20, 1.39, BoatHullType.MONOHULL, "L4.7"),
    LASER_RADIAL ("Laser Radial", true, 4.19, 1.41, BoatHullType.MONOHULL, "LAR", "Laser RAD", "RAD", "Radial"),
    LASER_INT ("Laser Int.", true, 4.19, 1.39, BoatHullType.MONOHULL, "Laser", "LSR"),
    LASER_SB3 ("Laser SB3", true, 6.15, 2.15, BoatHullType.MONOHULL, "LSB3", "SB20"),
    MELGES_24 ("Melges 24", true, 7.32, 2.50, BoatHullType.MONOHULL, "Melges-24"),
    MINI_TRANSAT ("Mini Transat 6.50", true, 6.50, 3.00, BoatHullType.MONOHULL, "Mini Transat"),
    MUSTO_SKIFF ("Musto Skiff", true, 4.55, 1.35, BoatHullType.MONOHULL, "Musto Performance Skiff", "MPS", "Musto"),
    NACRA_17 ("Nacra 17", true, 5.25, 2.59, BoatHullType.CATAMARAN, "N17", "Nacra-17"),
    OK ("OK Dinghy", true, 5.25, 2.59, BoatHullType.MONOHULL, "OK-Dinghy", "OK-Jolle", "OK"),
    OPTIMIST ("Optimist", true, 2.34, 1.07, BoatHullType.MONOHULL, "Opti", "Optimist Dinghy"),
    PLATU_25 ("Platu 25", true, 7.53, 2.62, BoatHullType.MONOHULL, "Platu", "Platu-25", "PLA", "B25"),
    RS_X ("RS:X", true, 2.86, 0.93, BoatHullType.SURFERBOARD, "RS-X", "RSX", "RS:X"),
    SONAR ("Sonar", true, 7.01, 2.39, BoatHullType.MONOHULL),
    STAR ("Star", true, 6.92, 1.74, BoatHullType.MONOHULL),
    STREAMLINE ("Streamline", true, 7.15, 2.55, BoatHullType.MONOHULL),
    SWAN_45 ("Swan 45", true, 13.83, 3.91, BoatHullType.MONOHULL, "Swan", "Swan-45"),
    //TODO: which boatclasses of swan are important (there are several ones)
    TORNADO ("Tornado Catamaran", true, 6.10, 3.02, BoatHullType.CATAMARAN, "Tornado", "Tornado Cat"),
    X_99 ("X-99", true, 9.96, 2.95, BoatHullType.MONOHULL, "X99"),
    CONTENDER ("Contender", true, 4.88, 1.42, BoatHullType.MONOHULL),
    FLYING_DUTCHMAN ("Flying Dutchman", true, 6.10, 1.80, BoatHullType.MONOHULL);

    private final String displayName;
    private final String[] alternativeNames;
    private final double hullLengthInMeter;
    private final double hullBeamInMeter;
    private final BoatHullType hullType;
    private final boolean typicallyStartsUpwind;

    private BoatClassMasterdata(String displayName, boolean typicallyStartsUpwind, double hullLengthInMeter,
            double hullBeamInMeter, BoatHullType hullType, String... alternativeNames) {
        this.displayName = displayName;
        this.typicallyStartsUpwind = typicallyStartsUpwind;
        this.hullLengthInMeter = hullLengthInMeter;
        this.hullBeamInMeter = hullBeamInMeter;
        this.hullType = hullType;
        this.alternativeNames = alternativeNames;
    }

    private BoatClassMasterdata(String displayName, boolean typicallyStartsUpwind, double hullLengthInMeter,
            double hullBeamInMeter, BoatHullType hullType) {
        this.displayName = displayName;
        this.typicallyStartsUpwind = typicallyStartsUpwind;
        this.hullLengthInMeter = hullLengthInMeter;
        this.hullBeamInMeter = hullBeamInMeter;
        this.hullType = hullType;
        this.alternativeNames = null;
    }

    public BoatClassMasterdata resolveBoatClass(String boatClassName) {
        for (BoatClassMasterdata boatClass : values()) {
            if (boatClass.displayName.toUpperCase().equals(boatClassName.toUpperCase())) {
                return boatClass;
            }
            for (String name : boatClass.alternativeNames) {
                if (name.toUpperCase().equals(boatClassName.toUpperCase())) {
                    return boatClass;
                }
            }
        }
        return null;
    }

    public Distance getHullLength() {
        return new MeterDistance(hullLengthInMeter);
    }

    public String getDisplayName() {
        return displayName;
    }

    public String[] getAlternativeNames() {
        return alternativeNames == null ? new String[0] : alternativeNames;
    }

    public Distance getHullBeam() {
        return new MeterDistance(hullBeamInMeter);
    }

    public BoatHullType getHullType() {
        return hullType;
    }

    public boolean isTypicallyStartsUpwind() {
        return typicallyStartsUpwind;
    }
}
