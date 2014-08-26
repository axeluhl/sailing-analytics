package buildstructure;

import com.sap.sailing.xrr.schema.Race;

public class GuessSeriesFromRaceName implements GuessSeriesStrategy {

	@Override
	public String guessSeries(String race) {
		String[] raceName = race.split(" ");
        String raceType = raceName[0];
        
        switch (raceType.charAt(0)){
            case 'Q':
                raceType = "Qualification";
                break;
            case 'R':
                raceType = "Opening Series";
                break;
            case 'F':
                raceType = "Finals";
                break;
            case 'M':
                raceType = "Medal";
                break;
            default:
                raceType = "Default";
                break;
        }
        return raceType;
	}

}
