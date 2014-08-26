package buildstructure;

import java.util.List;

import com.sap.sailing.xrr.schema.Race;

public class SetRacenumberFromSeries implements SetRacenumberStrategy{

	@Override
	public void setRacenumber(Race race, RaceType raceType, int i, List<String> raceNames) {
		if (race != null) {
            char beginOfRaceType = raceType.getFirstChar();
            if (beginOfRaceType =='D' && race.getFirstChar()!='R') {
            	beginOfRaceType = 'R';
            	raceNames.add("" + beginOfRaceType + (i+1));
            } else {
            	raceNames.add(race.getRaceName());
            }
        }
	}

}
