package com.sap.sailing.xrr.structureimport.buildstructure;


import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.xrr.schema.Race;

public class RegattaStructure {
    List<Series> series = new ArrayList<Series>();
    GuessSeriesStrategy guessSeriesStrategy;
    
    public RegattaStructure(GuessSeriesStrategy guessSeries) {
        this.guessSeriesStrategy = guessSeries;
    }

    public void addRace(Race race, int[] numberOfRaces) {
        final String oneSeries = guessSeriesStrategy.guessSeries(race.getRaceName());
        boolean added = false;
        for (Series singleSeries : series) {
            if (singleSeries.getSeries().equals(oneSeries)) {
                singleSeries.addRace(race, numberOfRaces);
                added = true;
            }
        }
        if (!added) {
            Series newSeries = new Series(oneSeries);
            newSeries.addRace(race, numberOfRaces);
            series.add(newSeries);
        }
    }
    
    public Iterable<Series> getSeries() {
        return series;
    }

    public void checkSeries() {
        if (series.size() == 1) {
            series.iterator().next().setSeries("Default");
        }
    }
}
