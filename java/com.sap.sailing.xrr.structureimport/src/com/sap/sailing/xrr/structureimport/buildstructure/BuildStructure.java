package com.sap.sailing.xrr.structureimport.buildstructure;

import java.util.Set;

import com.sap.sailing.xrr.schema.Race;

public class BuildStructure {
    private RegattaStructure structure = null;

    public BuildStructure(Iterable<Race> races) {
        int[] numberOfRaces = new int[10];
        for (Race race : races) {
            int raceNumber = race.getRaceNumber().intValue();
            if (raceNumber > numberOfRaces.length) {
                // Array erweitern
                int[] temp = numberOfRaces;
                numberOfRaces = new int[numberOfRaces.length + 10];
                for (int j = 0; j < temp.length; j++) {
                    numberOfRaces[j] = temp[j];
                }
                numberOfRaces[raceNumber - 1] += 1;
            } else {
                numberOfRaces[raceNumber - 1] += 1;
            }
        }
        structure = new RegattaStructure(new GuessSeriesFromRaceName());
        for (Race race : races) {
            structure.addRace(race, numberOfRaces);
        }
        structure.checkSeries();
    }

    public RegattaStructure getRegattaStructure() {
        return structure;
    }
}
