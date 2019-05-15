package com.sap.sailing.xrr.structureimport.buildstructure;

import java.awt.Color;
import java.util.Set;
import java.util.TreeSet;

import com.sap.sailing.xrr.schema.Race;

public class Fleet implements Comparable {
    private final int NUMBEROFRACES = 10;

    private String color = "";
    private Race[] racesOld = new Race[NUMBEROFRACES];
    private Set<Race> races = new TreeSet<Race>();
    private int numRaces = 0;

    public Fleet(String color) {
        this.color = color;
    }

    public void addRace(Race race) {
        String raceName = race.getRaceName();
        if (raceName.split(" ").length > 1) {
            raceName = raceName.split(" ")[0];
        }
        if (raceName.equals("F0")) {
            raceName = "CF";
        }
        race.setRaceName(raceName);

        int raceNumber = Integer.parseInt(race.getRaceNumber() + "");
        if (racesOld.length > raceNumber) {
            racesOld[raceNumber - 1] = race;
            numRaces++;
        } else {
            Race[] temp = racesOld;
            racesOld = new Race[raceNumber];
            for (int i = 0; i < temp.length; i++) {
                racesOld[i] = temp[i];
            }
            racesOld[raceNumber - 1] = race;
            numRaces++;
        }
        races.add(race);
    }

    public Iterable<Race> getRaces() {
        return races;
    }

    public String getColor() {
        return color;
    }

    @Override
    public int compareTo(Object o) {
        int a = getValueOfColor(this.color);
        int b = getValueOfColor(((Fleet) o).getColor());
        int result = 0;
        if (a < b) {
            result = -1;
        } else if (a > b) {
            result = 1;
        }
        return result;
    }

    private int getValueOfColor(String color) {
        int value = 0;

        switch (color) {
        case "Gold":
            value = 0;
            break;
        case "Silver":
            value = 1;
            break;
        case "Bronze":
            value = 2;
            break;
        case "Emerald":
            value = 3;
            break;
        case "Blue":
            value = 0;
            break;
        case "Red":
            value = 1;
            break;
        case "Green":
            value = 2;
            break;
        case "Yellow":
            value = 3;
            break;
        default:
            value = 10;
            break;
        }

        return value;
    }

    public int getNumRaces() {
        return numRaces;
    }

}
