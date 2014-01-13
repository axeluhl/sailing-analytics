package com.sap.sailing.domain.markpassingcalculation;

import com.sap.sailing.domain.tracking.Wind;

public class MockedPolarSheetDeliverer {
    public static double reaching;
    public static double up;
    public static double down;
    
    double getReaching(Wind w){
        return reaching;
    };
    
    double getUpwind(Wind w){
      return up;  
    };
     
    double getDownwind(Wind w){
        return down;
    };
}
