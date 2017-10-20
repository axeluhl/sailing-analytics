package com.sap.sse.pairinglist.impl;

import java.util.ArrayList;

import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;

public class PairingListTemplateImpl<Flight,Group,Competitor> implements PairingListTemplate<Flight,Group,Competitor> {
    
    private int[][] pairingListTemplate;
    private double standardDev;
    
    public PairingListTemplateImpl(PairingFrameProvider<Flight,Group,Competitor> pPFP) {
        pairingListTemplate= new int[pPFP.getGroupsCount()][pPFP.getCompetitorCount()/pPFP.getGroupsCount()];
        this.create(pPFP.getFlightsCount(), pPFP.getGroupsCount(), pPFP.getCompetitorCount() );
    }


    @Override
    public double getQualitiy() {
        
        return 0;
    }

    @Override
    public PairingList<Flight, Group, Competitor> createPairingList(
            PairingFrameProvider<Flight, Group, Competitor> pPFP) {
 
        return null;
    }
    
    private void create(int flights, int groups, int competitors){
        int[][] bestTeamAssociations = new int[competitors][competitors];
        int[][] bestPLT = new int[groups][competitors / groups];
        
        double bestDev = Double.POSITIVE_INFINITY;
        
        for(int iteration=0;iteration<100;iteration++){
            int[][] currentAssociations = new int[competitors][competitors];
            int[][] currentPLT = new int[groups][competitors / groups];
            for(int zFlight=0;zFlight<flights;zFlight++){
                
            }
        }
        
    }
}
