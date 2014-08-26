package buildstructure;


import java.util.ArrayList;

import com.sap.sailing.xrr.schema.Race;

public class RegattaStructure {
    
    ArrayList<RaceType> raceTypes = new ArrayList<RaceType>();
    
    public RegattaStructure(){
        
    }
    
    public void addRace(Race race, int[] numberOfRaces){
        
        String[] raceName = race.getRaceName().split(" ");
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
