package buildstructure;


import java.util.ArrayList;

import com.sap.sailing.xrr.schema.Race;

public class RegattaStructure {
    
    ArrayList<Series> series = new ArrayList<Series>();
    GuessSeriesStrategy guessSeriesStrategy;
    
    
    public RegattaStructure(GuessSeriesStrategy guessSeries){
        this.guessSeriesStrategy = guessSeries;
    }
    
    public void addRace(Race race, int[] numberOfRaces){
        
        String oneSeries = guessSeriesStrategy.guessSeries(race.getRaceName());

        boolean added = false;
        
        for(int i=0;i<series.size();i++){
            if(series.get(i).getSeries().equals(oneSeries)){
                series.get(i).addRace(race, numberOfRaces);
                added = true;
            }
        }
        
        if(!added){
            Series newSeries = new Series(oneSeries);
            newSeries.addRace(race, numberOfRaces);
            series.add(newSeries);
        }
        
    }
    
    public ArrayList<Series> getSeries(){
        return series;
    }
    public void checkSeries(){
        if(series.size()==1){
            series.get(0).setSeries("Default");
        }
    }
}
