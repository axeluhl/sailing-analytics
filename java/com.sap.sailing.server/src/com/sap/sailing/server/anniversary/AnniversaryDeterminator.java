package com.sap.sailing.server.anniversary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.sap.sailing.domain.anniversary.AnniversaryRaceInfo;
import com.sap.sailing.domain.persistence.MongoObjectFactory;
import com.sap.sailing.server.anniversary.AnniversaryCalculator.ChangeListener;

public class AnniversaryDeterminator implements ChangeListener {
    private static int[] anniversaries = new int[] { 10, 25, 50, 75 };
    ConcurrentHashMap<Integer, AnniversaryRaceInfo> knownAnniversaries = new ConcurrentHashMap<>();
    private MongoObjectFactory mongoObjectFactory;

    public AnniversaryDeterminator(MongoObjectFactory mongoObjectFactory) {
        this.mongoObjectFactory = mongoObjectFactory;
        knownAnniversaries.putAll(mongoObjectFactory.getAnniversaryData());
    }

    @Override
    public void onChange(Collection<AnniversaryRaceInfo> collection) {
        ArrayList<AnniversaryRaceInfo> allRaces = new ArrayList<>(collection);
        Collections.sort(allRaces, new Comparator<AnniversaryRaceInfo>() {

            @Override
            public int compare(AnniversaryRaceInfo o1, AnniversaryRaceInfo o2) {
                return o1.getStartOfRace().compareTo(o2.getStartOfRace());
            }
        });
        boolean requiresPersist = checkForAnnivesaries(allRaces);
        if(requiresPersist){
            mongoObjectFactory.storeAnniversaryData(knownAnniversaries);
        }
    }

    private boolean checkForAnnivesaries(List<AnniversaryRaceInfo> collection) {
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

    private boolean onAnniversaryTraverse(int anniversaryToCheck, AnniversaryRaceInfo anniversaryRaceInfo) {
        AnniversaryRaceInfo knownAnniversary = knownAnniversaries.get(anniversaryToCheck);
        if (knownAnniversary != null) {
            System.out.println("Skipping anniversary " + anniversaryToCheck + " as it is already known");
            return false;
        } else {
            System.out.println("New anniversary " + anniversaryToCheck + " " + anniversaryRaceInfo);
            knownAnniversaries.put(anniversaryToCheck, anniversaryRaceInfo);
            return true;
        }
    }
}
