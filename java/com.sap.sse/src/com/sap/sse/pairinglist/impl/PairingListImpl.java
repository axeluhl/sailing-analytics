package com.sap.sse.pairinglist.impl;

import java.util.ArrayList; 
import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingList;

public class PairingListImpl<Flight, Group, Competitor> implements PairingList<Flight, Group, Competitor>  {
    
    private ArrayList<ArrayList<ArrayList<Competitor>>> pList;
    private int[][] pairingListTemplate;
    private PairingFrameProvider<Flight, Group, Competitor> frameProvider;
    
    /**
     * @param pList: pairing list with specific information of flights, groups and competitors
     * @param standardDev: describes quality of our pList (the lower the standardDev, the better the pairing list)
     */
    
    public PairingListImpl(int[][] template,PairingFrameProvider<Flight, Group, Competitor> pPFP) {
        this.pairingListTemplate = template;
        frameProvider = pPFP;
    }
    
    @Override
    public Iterable<Competitor> getCompetitors(Flight pFlight, Group pGroup) {
        ArrayList<Competitor> flight = new ArrayList<>();
        flight = this.pList.get(this.pList.indexOf(pFlight)).get(this.pList.indexOf(pGroup));
        Iterable<Competitor> iterable = flight;
        return iterable;
    }

    @Override
    public PairingFrameProvider<Flight, Group, Competitor> getProvider() {
        return frameProvider;
    }
    
    /**
     * SETTERS
     */

    public void setPList(ArrayList<ArrayList<ArrayList<Competitor>>> pList) {
        this.pList = pList;
    }
    
    /*
     * GETTERS 
     */
    
    public ArrayList<ArrayList<ArrayList<Competitor>>> getPList() {
        return this.pList;
    }
}
