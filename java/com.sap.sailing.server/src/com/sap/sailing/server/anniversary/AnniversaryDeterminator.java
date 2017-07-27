package com.sap.sailing.server.anniversary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.anniversary.AnniversaryRaceInfo;
import com.sap.sailing.server.anniversary.AnniversaryCalculator.ChangeListener;

public class AnniversaryDeterminator implements ChangeListener {
    private static int[] anniversaries = new int[]{10,25, 50,75};
    ConcurrentHashMap<Integer, AnniversaryRaceInfo> knownAnniversaries = new ConcurrentHashMap<>();

    public AnniversaryDeterminator() {
        knownAnniversaries.putAll(load());
    }

    @Override
    public void onChange(Collection<AnniversaryRaceInfo> collection) {
        ArrayList<AnniversaryRaceInfo> allRaces = new ArrayList<>(collection);
        Collections.sort(allRaces,new Comparator<AnniversaryRaceInfo>() {

            @Override
            public int compare(AnniversaryRaceInfo o1, AnniversaryRaceInfo o2) {
                return o1.getStartOfRace().compareTo(o2.getStartOfRace());
            }
        });
        checkForAnnivesaries(allRaces);
    }

    private void checkForAnnivesaries(List<AnniversaryRaceInfo> collection) {
        int newAmount = collection.size();
        int factor = 1;
        while(true){
            for(int anniversary:anniversaries){
                int anniversaryToCheck = anniversary*factor;
                if(anniversaryToCheck > newAmount){
                    return;
                }
                if(newAmount<anniversaryToCheck){
                    onAnniversaryTraverse(anniversaryToCheck,collection.get(anniversaryToCheck));
                }
            }
        }        
    }

    private void onAnniversaryTraverse(int anniversaryToCheck, AnniversaryRaceInfo anniversaryRaceInfo) {
        AnniversaryRaceInfo knownAnniversary = knownAnniversaries.get(anniversaryToCheck);
        if(knownAnniversary != null){
            System.out.println("Skipping anniversary " + anniversaryToCheck + " as it is already known");
        }else{
            System.out.println("New anniversary " + anniversaryToCheck + " " + anniversaryRaceInfo);
            knownAnniversaries.put(anniversaryToCheck, anniversaryRaceInfo);
            persist();
        }
    }

    private void persist() {
        // TODO Auto-generated method stub
    }
    
    
    private Map<Integer, AnniversaryRaceInfo> load() {
        return Collections.emptyMap();
    }
}
