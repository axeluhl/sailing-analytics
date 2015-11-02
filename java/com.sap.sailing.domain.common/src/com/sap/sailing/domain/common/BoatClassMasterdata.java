package com.sap.sailing.domain.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sap.sailing.domain.common.impl.MeterDistance;


public enum BoatClassMasterdata {
    _18Footer ("18Footer", true, 8.90, 2.00, BoatHullType.MONOHULL, true, "18.Footer", "18ft", "18ft Skiff", "18. Footer"),
    _2_4M ("2.4 Meter", true, 4.11, 0.81, BoatHullType.MONOHULL, false, "2.4M", "2.4mR", "2.4 Metre", "2.4-metre", "24MR"),
    _5O5 ("5O5", true, 5.03, 1.88, BoatHullType.MONOHULL, true, "505", "5o5", "505er", "5o5er"),
    _12M ("12 Meter", true, 5.97, 1.43, BoatHullType.MONOHULL, true, "12M", "12mR", "12SQM", "12-metre", "12 metre"),
    _29ER ("29er", true, 4.45, 1.70, BoatHullType.MONOHULL, true),
    _49ER ("49er", true, 4.88, 1.93, BoatHullType.MONOHULL, true),
    _49ERFX ("49erFX", true, 4.88, 1.93, BoatHullType.MONOHULL, true, "49FX"),
    _420 ("420", true, 4.20, 1.65, BoatHullType.MONOHULL, true, "420er", "420M", "420W"),
    _470 ("470", true, 4.70, 1.68, BoatHullType.MONOHULL, true, "470er", "470M", "470W"),
    RS800 ("RS 800", true, 4.80, 1.88, BoatHullType.MONOHULL, true, "RS800"),
    A_CAT ("A-Catamaran", true, 5.49, 2.30, BoatHullType.CATAMARAN, false, "A-Cat", "ACat", "A-Class Catamaran"),
    ALBIN_EXPRESS ("Albin Express", true, 7.77, 2.50, BoatHullType.MONOHULL, false),
    ALBIN_BALLAD ("Albin Ballad", true, 9.12, 2.95, BoatHullType.MONOHULL, false),
    B_ONE ("B/ONE", true, 7.80, 2.49, BoatHullType.MONOHULL, true, "B-ONE"),
    CANOE_IC ("International Canoe", true, 5.20, 1.01, BoatHullType.MONOHULL, true, "Canoe IC", "Canoe-IC", "IC", "Kanu IC", "International Canoe", "International 10 Sq.m. Sailing Canoe"),
    CANOE_TAIFUN ("Canoe Taifun", true, 5.20, 1.32, BoatHullType.MONOHULL, false, "Taifun", "Taifun Kanu", "Kanu Taifun"),
    CONTENDER ("Contender", true, 4.88, 1.42, BoatHullType.MONOHULL, false),
    D_ONE ("D-One", true, 4.23, 2.31, BoatHullType.MONOHULL, true, "Devoti D-One", "DOne", "D_One"),
    DRAGON_INT ("Dragon Int.", true, 8.89, 1.96, BoatHullType.MONOHULL, true, "Drachen", "Dragon"),
    DYAS("Dyas", true, 7.15, 1.95, BoatHullType.MONOHULL, true),
    EXTREME_40 ("Extreme 40", false, 12.2, 6.60, BoatHullType.CATAMARAN, true, "Extreme-40", "Extreme40", "ESS40"),
    D_35 ("D35", false, 10.81, 6.89, BoatHullType.CATAMARAN, false),
    ELLIOTT_6M ("Elliott 6m", true, 6.0, 2.35, BoatHullType.MONOHULL, true, "Elliott6m"),
    EUROPE_INT ("Europe Int.", true, 3.35, 1.35, BoatHullType.MONOHULL, false, "Europe"),
    F_18 ("Formula 18", true, 6.85, 2.25, BoatHullType.CATAMARAN, true, "F18", "F-18"),
    FARR_30 ("Farr 30", true, 9.42, 3.08, BoatHullType.MONOHULL, true, "F30", "F-30", "Farr-30"),
    FINN ("Finn", true, 4.50, 1.51, BoatHullType.MONOHULL, false),
    FLYING_DUTCHMAN ("Flying Dutchman", true, 6.10, 1.80, BoatHullType.MONOHULL, true),
    FLYING_PHANTOM ("Flying Phantom", true, 5.52, 3.00, BoatHullType.CATAMARAN, true),
    FOLKBOAT ("Folkboat", true, 7.68, 2.20, BoatHullType.MONOHULL, false, "Folke", "Folkeboot"),
    FUN ("FUN", true, 7.20, 2.45, BoatHullType.MONOHULL, true, "FUN O.D.", "FUN OD", "Open FUN"),
    F_16 ("Formula 16", true, 5.00, 2.50, BoatHullType.CATAMARAN, true, "F16", "F-16"),
    HOBIE_16 ("Hobie 16", true, 5.05, 2.41, BoatHullType.CATAMARAN, false, "H16"),
    H_BOAT ("H-Boat", true, 8.28, 2.18, BoatHullType.MONOHULL, true, "HB"),
    HOBIE_TIGER ("Hobie Tiger", true, 5.51, 2.60, BoatHullType.CATAMARAN, true),
    HOBIE_WILD_CAT ("Hobie Wild Cat", true, 5.49, 2.59, BoatHullType.CATAMARAN, true, "Hobie Wild Cat F18"),
    INTERNATIONAL_14("International 14", true, 4.27, 1.83, BoatHullType.MONOHULL, true, "I14", "Int.14", "Int14"),
    J22 ("J/22", true, 6.86, 2.44, BoatHullType.MONOHULL, true, "J22", "J-22"),
    J24 ("J/24", true, 7.32, 2.67, BoatHullType.MONOHULL, true, "J24", "J-24"),
    J70 ("J/70", true, 6.93, 2.25, BoatHullType.MONOHULL, true, "J70", "J-70"),
    J80 ("J/80", true, 8.0, 2.51, BoatHullType.MONOHULL, true, "J80", "J-80"),
    KIELZUGVOGEL ("Kielzugvogel", true, 5.80, 1.88, BoatHullType.MONOHULL, false, "KZV"), 
    KITE ("Kite", true, 3.35, 1.52, BoatHullType.MONOHULL, false), 
    LASER_2 ("Laser 2", true, 4.39, 1.42, BoatHullType.MONOHULL, false, "Laser II", "Laser2", "Laser-2", "Laser-II"),
    LASER_4_7 ("Laser 4.7", true, 4.20, 1.39, BoatHullType.MONOHULL, false, "L4.7"),
    LASER_RADIAL ("Laser Radial", true, 4.19, 1.41, BoatHullType.MONOHULL, false, "LAR", "Laser RAD", "RAD", "Radial"),
    LASER_INT ("Laser Int.", true, 4.19, 1.39, BoatHullType.MONOHULL, false, "Laser", "LSR"),
    LASER_SB3 ("Laser SB3", true, 6.15, 2.15, BoatHullType.MONOHULL, false, "LSB3", "SB20"),
    LAGO_26 ("Lago 26", true, 7.95, 2.50, BoatHullType.MONOHULL, true, "Lago26"),
    MELGES_24 ("Melges 24", true, 7.32, 2.50, BoatHullType.MONOHULL, true, "Melges-24", "M24"),
    MINI_TRANSAT ("Mini Transat 6.50", true, 6.50, 3.00, BoatHullType.MONOHULL, true, "Mini Transat"),
    MUSTO_SKIFF ("Musto Skiff", true, 4.55, 1.35, BoatHullType.MONOHULL, true, "Musto Performance Skiff", "MPS", "Musto"),
    NACRA_17 ("Nacra 17", true, 5.25, 2.59, BoatHullType.CATAMARAN, true, "N17", "Nacra-17"),
    O_JOLLE ("O-Jolle", true, 5.00, 1.66, BoatHullType.MONOHULL, false, "O Jolle", "OJolle", "Olympiajolle"),
    OK ("OK Dinghy", true, 5.25, 2.59, BoatHullType.MONOHULL, false, "OK-Dinghy", "OK-Jolle", "OK"),
    OPEN_BIC("O'pen BIC", true, 2.75, 1.14, BoatHullType.MONOHULL, false, "OpenBIC"),
    OPTIMIST ("Optimist", true, 2.34, 1.07, BoatHullType.MONOHULL, false, "Opti", "Optimist Dinghy"),
    PIRATE ("Pirate", true, 5.00, 1.61, BoatHullType.MONOHULL, false, "Pirat", "Piraten"),
    PLATU_25 ("Platu 25", true, 7.53, 2.62, BoatHullType.MONOHULL, true, "Platu", "Platu-25", "PLA", "B25"),
    PWA ("PWA", true, 5.0, 0.6, BoatHullType.MONOHULL, true, "Professional Windsurfers Association", "PWA World Tour"),
    RC44 ("RC44", true, 13.35, 2.75, BoatHullType.MONOHULL, true),
    RS_X ("RS:X", true, 2.86, 0.93, BoatHullType.SURFERBOARD, false, "RS-X", "RSX", "RS:X"),
    RS_FEVA ("RS Feva", true, 3.64, 1.42, BoatHullType.MONOHULL, true, "RSFeva"),
    SONAR ("Sonar", true, 7.01, 2.39, BoatHullType.MONOHULL, true),
    SOLING ("Soling", true, 8.15, 1.91, BoatHullType.MONOHULL, true),
    STAR ("Star", true, 6.92, 1.74, BoatHullType.MONOHULL, false, "STR"),
    STREAMLINE ("Streamline", true, 7.15, 2.55, BoatHullType.MONOHULL, true),
    SWAN_45 ("Swan 45", true, 13.83, 3.91, BoatHullType.MONOHULL, true, "Swan", "Swan-45"),
    TORNADO ("Tornado Catamaran", true, 6.10, 3.02, BoatHullType.CATAMARAN, true, "Tornado", "Tornado Cat"),
    TRIAS ("Trias", true, 9.20, 2.12, BoatHullType.MONOHULL, true),
    X_99 ("X-99", true, 9.96, 2.95, BoatHullType.MONOHULL, true, "X99"),
    
    // multi-class "boat classes"; to be replaced at some later point in time by something like a RegattaClass
    ORC ("ORC", true, 13.83, 3.91, BoatHullType.MONOHULL, true),
    ORC_CLUB ("ORC Club", true, 13.83, 3.91, BoatHullType.MONOHULL, true),
    ORC_INTERNATIONAL ("ORC International", true, 13.83, 3.91, BoatHullType.MONOHULL, true, "ORC Int.");

    private final String displayName;
    private final String[] alternativeNames;
    private final double hullLengthInMeter;
    private final double hullBeamInMeter;
    private final BoatHullType hullType;
    private final boolean typicallyStartsUpwind;
    private final boolean hasAdditionalDownwindSail;

    private static Map<String, BoatClassMasterdata> fromUnifiedDisplayAndAlternativeNamesToBoatClassMasterdata; 

    private BoatClassMasterdata(String displayName, boolean typicallyStartsUpwind, double hullLengthInMeter,
            double hullBeamInMeter, BoatHullType hullType, boolean hasAdditionalDownwindSail, String... alternativeNames) {
        this.displayName = displayName;
        this.typicallyStartsUpwind = typicallyStartsUpwind;
        this.hullLengthInMeter = hullLengthInMeter;
        this.hullBeamInMeter = hullBeamInMeter;
        this.hullType = hullType;
        this.hasAdditionalDownwindSail = hasAdditionalDownwindSail;
        this.alternativeNames = alternativeNames;
        addToCache(this);
    }

    private void addToCache(BoatClassMasterdata boatClassMasterdata) {
        if (fromUnifiedDisplayAndAlternativeNamesToBoatClassMasterdata == null) {
            fromUnifiedDisplayAndAlternativeNamesToBoatClassMasterdata = new HashMap<>();
        }
        fromUnifiedDisplayAndAlternativeNamesToBoatClassMasterdata.put(unifyBoatClassName(getDisplayName()), this);
        for (final String alternativeName : getAlternativeNames()) {
            fromUnifiedDisplayAndAlternativeNamesToBoatClassMasterdata.put(unifyBoatClassName(alternativeName), this);
        }
    }

    private BoatClassMasterdata(String displayName, boolean typicallyStartsUpwind, double hullLengthInMeter,
            double hullBeamInMeter, BoatHullType hullType, boolean hasAdditionalDownwindSail) {
        this.displayName = displayName;
        this.typicallyStartsUpwind = typicallyStartsUpwind;
        this.hullLengthInMeter = hullLengthInMeter;
        this.hullBeamInMeter = hullBeamInMeter;
        this.hullType = hullType;
        this.hasAdditionalDownwindSail = hasAdditionalDownwindSail;
        this.alternativeNames = null;
        addToCache(this);
    }

    public static BoatClassMasterdata resolveBoatClass(String boatClassName) {
        return fromUnifiedDisplayAndAlternativeNamesToBoatClassMasterdata.get(unifyBoatClassName(boatClassName));
    }

    /**
     * Maps the <code>boatClassName</code> string by removing all whitespace and converting to all upper case.
     * Example: "Laser Int." becomes "LASERINT."<p>
     * 
     * Note that the mapping is not related to the set of {@link BoatClassMasterdata} objects known and works the same
     * regardless of whether <code>boatClassName</code> matches any of the existing {@link BoatClassMasterdata} literals,
     * display names or alternative names.
     */
    public static String unifyBoatClassName(String boatClassName) {
        return boatClassName == null ? null : boatClassName.toUpperCase().replaceAll("\\s+","");
    }
    
    /**
     * If any of the existing {@link BoatClassMasterdata} objects has a matching {@link #unifyBoatClassName(String)
     * unified} display or alternative name, the unified display name of that object is returned. Otherwise, the
     * {@link #unifyBoatClassName(String) unified} <code>boatClassName</code> value is returned. Example: "LASER" and
     * "Laser" and "LSR" and "lsr" and "Laser Int." and "LASER INT ." and "LASERINT." will all be mapped to "LASERINT."
     * based on the boat class masterdata object whose display name is "Laser Int.". In turn, "xyz" and "x y z" will be
     * mapped to "XYZ" without any matching {@link BoatClassMasterdata} object existing, simply based on the string
     * mapping described for {@link #unifyBoatClassName(String)}.
     */
    public static String unifyBoatClassNameBasedOnExistingMasterdata(String boatClassName) {
        BoatClassMasterdata bcm = resolveBoatClass(boatClassName);
        final String result;
        if (bcm != null) {
            result = unifyBoatClassName(bcm.getDisplayName());
        } else {
            result = unifyBoatClassName(boatClassName);
        }
        return result;
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
    
    public boolean hasAdditionalDownwindSail() {
        return hasAdditionalDownwindSail;
    }

    public static Iterable<String> getAllBoatClassNames(boolean includeAlternativeNames) {
        final List<String> result = new ArrayList<>();
        for (BoatClassMasterdata bcmd : values()) {
            result.add(bcmd.getDisplayName());
            if (includeAlternativeNames) {
                for (String alternativeName : bcmd.getAlternativeNames()) {
                    result.add(alternativeName);
                }
            }
        }
        return result;
    }
}
