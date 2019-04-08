package com.sap.sailing.xrr.structureimport.buildstructure;

import com.sap.sailing.xrr.schema.Race;

public class GuessSeriesFromRaceName implements GuessSeriesStrategy {
    @Override
    public String guessSeries(String race) {
        String[] raceName = race.split(" ");
        String series = raceName[0];

        switch (series.charAt(0)) {
        case 'Q':
            series = "Qualification";
            break;
        case 'R':
            series = "Opening Series";
            break;
        case 'F':
            series = "Finals";
            break;
        case 'M':
            series = "Medal";
            break;
        default:
            series = "Default";
            break;
        }
        return series;
    }
}
