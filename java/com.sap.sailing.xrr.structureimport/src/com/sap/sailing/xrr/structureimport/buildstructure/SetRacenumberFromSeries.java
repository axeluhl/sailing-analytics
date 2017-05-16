package com.sap.sailing.xrr.structureimport.buildstructure;

import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.xrr.schema.Race;

public class SetRacenumberFromSeries implements SetRacenumberStrategy {

    @Override
    public void setRacenumber(Race race, Series series, int i) {
    	List<String> raceNames = series.getRaceNames();
    	if(raceNames == null){
    		raceNames = new ArrayList<String>();
    		series.setRaceNames(raceNames);
    	}
        if (race != null) {
            char beginOfSeries = series.getFirstChar();
            if (beginOfSeries == 'D' && race.getFirstChar() != 'R') {
                beginOfSeries = 'R';
                raceNames.add("" + beginOfSeries + (i + 1));
            } else {
                raceNames.add(race.getRaceName());
            }
        }
    }
}
