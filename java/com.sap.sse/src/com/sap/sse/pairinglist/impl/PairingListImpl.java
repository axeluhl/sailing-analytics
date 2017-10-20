package com.sap.sse.pairinglist.impl;

import java.util.ArrayList;
import java.util.Iterator;

import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingList;

public class PairingListImpl<Flight, Group, Competitor> implements PairingList<Flight, Group, Competitor>  {
    
    private ArrayList<ArrayList<ArrayList<Competitor>>> pList;
    private double standardDev;
    
    public PairingListImpl() {
        
    }
    
    /*
     * @param pList: pairing list with specific information of flights, groups and competitors
     * @param standardDev: describes quality of our pList (the lower the standardDev, the better the pairing list)
     */
    
    public PairingListImpl(ArrayList<ArrayList<ArrayList<Competitor>>> pList, double standardDev) {
        this.pList = pList;
        this.standardDev = standardDev;
    }
    
    @Override
    public Iterator<Competitor> getCompetitors(Flight pFlight, Group pGroup) {
        ArrayList<ArrayList<Competitor>> flight = new ArrayList<>();
        flight = this.pList.get(this.pList.indexOf(pFlight));
        return flight.get(flight.indexOf(pGroup)).iterator();
    }

    @Override
    public PairingFrameProvider<Object, Object, Object> getProvider() {
        return null;
    }
    
    /*
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

    public double getStandardDev() {
        return standardDev;
    }
}
