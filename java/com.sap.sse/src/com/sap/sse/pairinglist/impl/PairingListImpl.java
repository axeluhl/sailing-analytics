package com.sap.sse.pairinglist.impl;

import java.util.ArrayList;
import com.sap.sse.pairinglist.PairingList;

public class PairingListImpl implements PairingList {
    
    private ArrayList<ArrayList<ArrayList<Integer>>> pList;
    private double standardDev;
    
    public PairingListImpl() {
        
    }
    
    public PairingListImpl(ArrayList<ArrayList<ArrayList<Integer>>> pList, double standardDev) {
        this.pList = pList;
        this.standardDev = standardDev;
    }

    public void setPList(ArrayList<ArrayList<ArrayList<Integer>>> pList) {
        this.pList = pList;
    }
    
    public ArrayList<ArrayList<ArrayList<Integer>>> getPList() {
        return this.pList;
    }

    public double getStandardDev() {
        return standardDev;
    }
    
}
