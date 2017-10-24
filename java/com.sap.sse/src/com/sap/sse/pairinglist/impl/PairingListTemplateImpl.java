package com.sap.sse.pairinglist.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

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
    
}