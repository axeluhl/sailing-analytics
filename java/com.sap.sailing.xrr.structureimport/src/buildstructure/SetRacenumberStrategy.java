package buildstructure;

import java.util.List;

import com.sap.sailing.xrr.schema.Race;

public interface SetRacenumberStrategy {
	
	public void setRacenumber(Race race, RaceType raceType, int i, List<String> raceNames);

}
