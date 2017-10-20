package com.sap.sse.pairinglist.impl;

import java.util.ArrayList;

import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingList;
import com.sap.sse.pairinglist.PairingListTemplate;

/**
 * @author D070307
 *
 * @param <Flight>
 * @param <Group>
 * @param <Competitor>
 */
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
    
    /**
     * @param associations: association describes a 2 dimensional array of integers, which contains the information 
     *                      about how often the teams play against each other
     * @return standardDev: returns how much the association values deviate from each other
     */
    
    private double calcStandardDev(int[][] associations) {
        double standardDev = Double.POSITIVE_INFINITY;
        double expectedValue = 0;
        
        /** 
         * hist shows how often a specific value of one association occurs.
         * A value specifies how often one team plays against another.
         */
        ArrayList<Integer> hist = new ArrayList<>();     
        
        // filling hist
        for (int[] key: associations) {
            for (int value: key) {
                hist.add(value, (hist.get(value) + 1));
            }
        }
        
        // calculating the expected value of hist
        for (int i = 0; i < hist.size(); i++) {
            expectedValue = (hist.get(i) / hist.size()) * i; 
        }
        
        // calculating standard deviation from value and expectedValue
        for (Integer val: hist) {
            standardDev = standardDev + Math.pow((val - expectedValue), 2);
        }
        
        if (standardDev > 0) {
            standardDev = Math.sqrt(standardDev / hist.size());
        }
        
        return standardDev;
    }
}
