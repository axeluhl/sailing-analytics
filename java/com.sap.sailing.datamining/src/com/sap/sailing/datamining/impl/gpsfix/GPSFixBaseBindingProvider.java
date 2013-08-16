package com.sap.sailing.datamining.impl.gpsfix;

import groovy.lang.Binding;

import com.sap.sailing.datamining.BaseBindingProvider;
import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.domain.common.LegType;

public class GPSFixBaseBindingProvider implements BaseBindingProvider<GPSFixWithContext> {

    @Override
    public Binding createBaseBinding() {
        Binding binding = new Binding();
        binding.setProperty("LegType", new LegTypeWrapper());
        return binding;
    }
    
    @SuppressWarnings("unused")
    private static class LegTypeWrapper {

        public static LegType UPWIND = LegType.UPWIND;
        public static LegType DOWNWIND = LegType.DOWNWIND;
        public static LegType REACHING = LegType.REACHING;
        
    }

}
