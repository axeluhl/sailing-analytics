package buildstructure;

import java.util.List;

import com.sap.sailing.xrr.schema.Race;

public class SetRacenumberFromSeries implements SetRacenumberStrategy{

	@Override
	public void setRacenumber(Race race, Series series, int i, List<String> raceNames) {
		if (race != null) {
            char beginOfSeries = series.getFirstChar();
            if (beginOfSeries =='D' && race.getFirstChar()!='R') {
            	beginOfSeries = 'R';
            	raceNames.add("" + beginOfSeries + (i+1));
            } else {
            	raceNames.add(race.getRaceName());
            }
        }
	}

}
