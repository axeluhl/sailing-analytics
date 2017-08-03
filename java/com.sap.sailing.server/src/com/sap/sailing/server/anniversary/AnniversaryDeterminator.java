package com.sap.sailing.server.anniversary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.anniversary.AnniversaryRaceInfo;
import com.sap.sailing.domain.anniversary.SimpleAnniversaryRaceInfo;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.server.anniversary.AnniversaryCalculator.ChangeListener;

public class AnniversaryDeterminator implements ChangeListener {
    private static int[] anniversaries = new int[] { 10, 25, 50, 75 };
    ConcurrentHashMap<Integer, AnniversaryRaceInfo> knownAnniversaries = new ConcurrentHashMap<>();
    private MongoObjectFactory mongoObjectFactory;
    private AnniversaryCalculator anniversaryCalculator;

    public AnniversaryDeterminator(MongoObjectFactory mongoObjectFactory) {
        this.mongoObjectFactory = mongoObjectFactory;
        knownAnniversaries.putAll(mongoObjectFactory.getAnniversaryData());
    }

    @Override
    public void onChange(Collection<SimpleAnniversaryRaceInfo> collection) {
        ArrayList<SimpleAnniversaryRaceInfo> allRaces = new ArrayList<>(collection);
        Collections.sort(allRaces, new Comparator<SimpleAnniversaryRaceInfo>() {

            @Override
            public int compare(SimpleAnniversaryRaceInfo o1, SimpleAnniversaryRaceInfo o2) {
                return o1.getStartOfRace().compareTo(o2.getStartOfRace());
            }
        });
        boolean requiresPersist = checkForAnnivesaries(allRaces);
        if(requiresPersist){
            mongoObjectFactory.storeAnniversaryData(knownAnniversaries);
        }
    }

    private boolean checkForAnnivesaries(List<SimpleAnniversaryRaceInfo> collection) {
        int newAmount = collection.size();
        int factor = 1;
        boolean requiresPersist = false;
        while (true) {
            for (int anniversary : anniversaries) {
                int anniversaryToCheck = anniversary * factor;
                if (anniversaryToCheck > newAmount) {
                    return requiresPersist;
                }
                if (newAmount > anniversaryToCheck) {
                    boolean requiresPersistSingle = onAnniversaryTraverse(anniversaryToCheck, collection.get(anniversaryToCheck));
                    if(requiresPersistSingle){
                        requiresPersist = true;
                    }
                }
            }
        }
    }

    private boolean onAnniversaryTraverse(int anniversaryToCheck, SimpleAnniversaryRaceInfo simpleAnniversaryRaceInfo) {
        AnniversaryRaceInfo knownAnniversary = knownAnniversaries.get(anniversaryToCheck);
        if (knownAnniversary != null) {
            System.out.println("Skipping anniversary " + anniversaryToCheck + " as it is already known");
            return false;
        } else {
            System.out.println("New anniversary " + anniversaryToCheck + " " + simpleAnniversaryRaceInfo);
            AnniversaryRaceInfo fullData = anniversaryCalculator.getFullAnniversaryData(simpleAnniversaryRaceInfo);
            //get remote info here! how to do this?
            knownAnniversaries.put(anniversaryToCheck, fullData);
            return true;
        }
    }


    @Override
    public void setAnniversaryCalculator(AnniversaryCalculator anniversaryCalculator) {
        this.anniversaryCalculator = anniversaryCalculator;
    }
}
