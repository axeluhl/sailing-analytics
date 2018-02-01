package com.sap.sailing.gwt.autoplay.client.places.screens.preliveraceloop.racemap;

public class AdditionalData {
    float windInKts = 10.3f;
    int durationInSeconds = 320;
    float lengthInKm = 2;
    int legs = 5;
    int competitorCount = 15;
    String raceViewerURL = "URL from Settings";

    public float getWindInKts() {
        return windInKts;
    }

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public float getLengthInKm() {
        return lengthInKm;
    }

    public int getLegs() {
        return legs;
    }

    public String getRaceViewerURL() {
        return raceViewerURL;
    }

    public int getCompetitorCount() {
        return competitorCount;
    }
}
