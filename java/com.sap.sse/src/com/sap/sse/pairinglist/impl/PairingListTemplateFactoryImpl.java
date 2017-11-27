package com.sap.sse.pairinglist.impl;

import java.util.HashMap;
import java.util.Map;
import com.sap.sse.pairinglist.PairingFrameProvider;
import com.sap.sse.pairinglist.PairingListTemplate;
import com.sap.sse.pairinglist.PairingListTemplateFactory;

public class PairingListTemplateFactoryImpl implements PairingListTemplateFactory {
    private final Map<PairingFrameProvider,Map<Integer,PairingListTemplate>> pairingListTemplates;

    public PairingListTemplateFactoryImpl() {
       this.pairingListTemplates=new HashMap<>();
    }
//    public PairingListTemplateFactoryImpl(Map<PairingFrameProvider, PairingListTemplate> existingPairingListTemplates) {
//        this.pairingListTemplates = existingPairingListTemplates;
//    }
    
    @Override
    public PairingListTemplate getOrCreatePairingListTemplate(PairingFrameProvider pairingFrameProvider,int flightMultiplier) {
        PairingListTemplate result;
        try{
            result = pairingListTemplates.get(pairingFrameProvider).get(flightMultiplier);
        }catch(NullPointerException e){
            result=null;
        }
        if (result == null) {
            result = generatePairingList(pairingFrameProvider,flightMultiplier);
            if(pairingListTemplates.get(pairingFrameProvider)==null){
                pairingListTemplates.put(pairingFrameProvider, new HashMap<>());
                pairingListTemplates.get(pairingFrameProvider).put(flightMultiplier, result);
            }else{
                pairingListTemplates.get(pairingFrameProvider).put(flightMultiplier, result);
            }
        }
        return result;
    }
     /**
      * Creates a new ParingListTemplate which depends on the parameters of the given pairingFrameProvider.
      * @param pairingFrameProvider
      * @return new PariningListTemplate object
      */
    protected PairingListTemplate generatePairingList(PairingFrameProvider pairingFrameProvider,int flightMultiplier) {
        return new PairingListTemplateImpl(pairingFrameProvider,100000,flightMultiplier);
    }
}
