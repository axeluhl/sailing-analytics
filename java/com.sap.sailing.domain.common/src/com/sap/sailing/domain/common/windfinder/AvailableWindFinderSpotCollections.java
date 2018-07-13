package com.sap.sailing.domain.common.windfinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum AvailableWindFinderSpotCollections {
    KIELERFOERDE("kielerfoerde"),
    CHIEMSEE("chiemsee"),
    STARNBERGERSEE("starnbergersee"),
    WANNSEE("wannsee"),
    TRAVEMUENDE("travemuende"),
    BALTIC_OFFSHORE("baltic_offshore"),
    LITAUEN("litauen"),
    HAMBURG_ALSTER("hamburg_alster"),
    GDANSK("gdansk"),
    BREST("brest"),
    SZCZECIN("szczecin"),
    SANKT_PETERSBURG("sankt_petersburg"),
    SANKT_MORITZ("sankt_moritz"),
    PORTO_CERVO("porto_cervo"),
    MUEGGELSEE("mueggelsee");

    private final String name;

    private AvailableWindFinderSpotCollections(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<String> getAllAvailableWindFinderSpotCollectionsInAlphabeticalOrder() {
        List<String> result = new ArrayList<>();
        for (AvailableWindFinderSpotCollections awsc : values()) {
            result.add(awsc.getName());
        }
        Collections.sort(result);
        return result;
    }
}
