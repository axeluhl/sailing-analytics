package com.sap.sse.pairinglist.impl;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.PairingListTemplateFactory;

public class PairingListTemplateFactoryImpl implements PairingListTemplateFactory {

    private final Map<Integer, Map<Integer,PairingListTemplate>> pairingListTemplates;

    public PairingListTemplateFactoryImpl() {
       this.pairingListTemplates=new HashMap<>();
    }

    public PairingListTemplateFactoryImpl(Map<Integer, Map<Integer,PairingListTemplate>> existingPairingListTemplates) {
        this.pairingListTemplates = existingPairingListTemplates;
    }

    @Override
    public PairingListTemplate getOrCreatePairingListTemplate(PairingFrameProvider pairingFrameProvider,int flightMultiplier) {
        PairingListTemplate result;
        try{
            result = pairingListTemplates.get(pairingFrameProvider.getHashCode()).get(flightMultiplier);
        }catch(NullPointerException e){
            result=null;
        }

        if (result == null) {
            result = generatePairingList(pairingFrameProvider,flightMultiplier);
            if(pairingListTemplates.get(pairingFrameProvider.getHashCode())==null){
                pairingListTemplates.put(pairingFrameProvider.getHashCode(), new HashMap<>());
                pairingListTemplates.get(pairingFrameProvider.getHashCode()).put(flightMultiplier, result);
            }else{
                pairingListTemplates.get(pairingFrameProvider.getHashCode()).put(flightMultiplier, result);
            }
        }
        return result;
    }
     /**
      * Creates a new ParingListTemplate which depends on the parameters of the given pairingFrameProvider.
      * @param pairingFrameProvider
      * @return new PariningListTemplate object
      */
    protected PairingListTemplate generatePairingList(PairingFrameProvider pairingFrameProvider, int iterations,
            int flightMultiplier) {
        return new PairingListTemplateImpl(pairingFrameProvider, iterations, flightMultiplier);
    }

    protected PairingListTemplate generatePairingList(PairingFrameProvider pairingFrameProvider, int flightMultiplier) {
        return this.generatePairingList(pairingFrameProvider, 100000, flightMultiplier);
    }
    
    protected PairingListTemplate generatePairingList(PairingFrameProvider pairingFrameProvider) {
        return this.generatePairingList(pairingFrameProvider, 100000, 0);
    }
}
