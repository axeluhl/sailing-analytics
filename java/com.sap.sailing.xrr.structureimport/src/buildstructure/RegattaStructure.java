package buildstructure;


import java.util.ArrayList;

import com.sap.sailing.xrr.schema.Race;

public class RegattaStructure {
    
    ArrayList<RaceType> raceTypes = new ArrayList<RaceType>();
    GuessSeries guessSeries;
    
    public RegattaStructure(GuessSeries guessSeries){
        this.guessSeries = guessSeries;
    }
    
    public void addRace(Race race, int[] numberOfRaces){
        
        String raceType = guessSeries.guessSeries(race.getRaceName());

        boolean added = false;
        
        for(int i=0;i<raceTypes.size();i++){
            if(raceTypes.get(i).getRaceType().equals(raceType)){
                raceTypes.get(i).addRace(race, numberOfRaces);
                added = true;
            }
        }
        
        if(!added){
            RaceType newRaceType = new RaceType(raceType);
            newRaceType.addRace(race, numberOfRaces);
            raceTypes.add(newRaceType);
        }
        
    }
    public ArrayList<RaceType> getRaceTypes(){
        return raceTypes;
    }
    public void checkRaceTypes(){
        if(raceTypes.size()==1){
            raceTypes.get(0).setRaceType("Default");
        }
    }
}
