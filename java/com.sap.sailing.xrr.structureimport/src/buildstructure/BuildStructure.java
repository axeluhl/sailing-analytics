package buildstructure;


import java.util.List;

import com.sap.sailing.xrr.schema.Race;


public class BuildStructure {
    
    RegattaStructure structure = null;
    
    public BuildStructure(List<Race> races){
        
        int[] numberOfRaces = new int[10];
        
        for(int i=0;i<races.size();i++){
        	int raceNumber = races.get(i).getRaceNumber().intValue();
        	if(raceNumber > numberOfRaces.length){
        		//Array erweitern
        		int[] temp = numberOfRaces;
        		numberOfRaces = new int[numberOfRaces.length + 10];
        		for(int j=0;j<temp.length;j++){
        			numberOfRaces[j] = temp[j];
        		}
        		numberOfRaces[raceNumber-1] += 1;
        		
           	}else{
           		numberOfRaces[raceNumber-1] += 1;
           	}
        	
        }
        
        
        structure = new RegattaStructure(new GuessSeriesFromRaceName());
        
        for(int i=0;i<races.size();i++){
            structure.addRace(races.get(i), numberOfRaces);
        }
        
        structure.checkRaceTypes();
        
    }
    public RegattaStructure getRegattaStructure(){
        return structure;
    }
    
}
