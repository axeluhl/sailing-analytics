package com.sap.sse.pairinglist.impl;

import java.util.ArrayList;
import java.util.HashMap;

import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingList;

public class PairingListImpl<Flight, Group, Competitor> implements PairingList<Flight, Group, Competitor>  {
    
    private HashMap<Flight, HashMap<Group, ArrayList<Competitor>>> pList;
    private PairingListTemplateImpl<Flight, Group, Competitor> pairingListTemplate;
    private PairingFrameProvider<Flight, Group, Competitor> frameProvider;
    
    /**
     * @param pList: pairing list with specific information of flights, groups and competitors
     * @param standardDev: describes quality of our pList (the lower the standardDev, the better the pairing list)
     */
    
    public PairingListImpl(PairingListTemplateImpl<Flight, Group, Competitor> template,PairingFrameProvider<Flight, Group, Competitor> pPFP) {
        this.pairingListTemplate = template;
        this.frameProvider = pPFP;
        
        this.initializePairingList();
    }
    
    @Override
    public ArrayList<Competitor> getCompetitors(Flight pFlight, Group pGroup) {
        return this.pList.get(pFlight).get(pGroup);
    }

    @Override
    public PairingFrameProvider<Flight, Group, Competitor> getProvider() {
        return frameProvider;
    }
    
    private void initializePairingList() {
        this.pList = new HashMap<>();
        
        int[][] template = this.pairingListTemplate.getPairingListTemplate();
        
        ArrayList<Competitor> competitorList = new ArrayList<>();
        for (Competitor competitor : this.frameProvider.getCompetitors()) {
            competitorList.add(competitor);
        }
     
        int iGroup = 0;
        
        for (Flight flight: this.frameProvider.getFlights()) {            
            HashMap<Group, ArrayList<Competitor>> groupMap = new HashMap<>();
            
            for (Group group: this.frameProvider.getGroups(flight)) {                
                ArrayList<Competitor> compList = new ArrayList<>();
                
                for (int iCompInGroup = 0; iCompInGroup < this.frameProvider.getCompetitorCount() 
                        / this.frameProvider.getGroupsCount(); iCompInGroup++) {
                    int currentComp = template[iGroup][iCompInGroup];
                    
                    compList.add(iCompInGroup, competitorList.get(currentComp));
                }
                
                groupMap.put(group, compList);
                iGroup++;
            }
            
            this.pList.put(flight, groupMap);
        }
    }

    public HashMap<Flight, HashMap<Group, ArrayList<Competitor>>> getpList() {
        return pList;
    }

    public PairingListTemplateImpl<Flight, Group, Competitor> getPairingListTemplate() {
        return pairingListTemplate;
    }

    public PairingFrameProvider<Flight, Group, Competitor> getFrameProvider() {
        return frameProvider;
    }
}
