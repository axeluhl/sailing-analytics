package com.sap.sailing.gwt.ui.adminconsole;

public enum BoatTypes {
    RS_X ("RS:X"),
    RS_XY ("RS:X-Y"),
    LASER ("Laser"),
    LASER_RADIAL ("Laser Radial"),
    LASER_SB3 ("Laser SB3"),
    FINN ("Finn"),
    EUROPE ("Europe"),
    A_CAT ("A-Cat"),
    CONTENDER ("Contender"),
    STAR ("Star"),
    OK_DINGHY ("OK-Dinghy"),
    FLYING_DUTCHMAN ("Flying Dutchman"),
    FOLKEBOOT ("Folkeboot"),
    FORMULA ("Formula"),
    H_BOOT ("H-Boot"),
    HOBIE ("Hobie"),
    CLASS_420 ("420"),
    CLASS_470 ("470"),
    CLASS_505 ("505"),
    CLASS_29er("29er"),
    CLASS_49er("49er"),
    CLASS_24mR("2.4mR open"),
    CLASS_J_24 ("J-24"),
    CLASS_X_35 ("X-35"),
    CLASS_X_99 ("X-99"),
    SWAN_45 ("Swan-45"),
    ORC_I ("ORC I"),
    ORC_II ("ORC II"),
    ORC_III ("ORC II"),
    ORC_IV ("ORC IV"),
    MULTIHALL ("Multihull"),
    ALBIN_BALLAD ("Albin Ballad"),
    ALBIN_EXPRESS ("Albin Express"),
    CB66 ("CB66"),
    J_80 ("J/80"),
    MELGES24 ("Melges 24"),
    PLATU25 ("Platu25"),
    STREAMLINE ("Steamline"),
    ANY_CLASS ("Any class");
    
    private String name;
    
    BoatTypes(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
