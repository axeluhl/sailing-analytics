package com.sap.sailing.domain.markpassingcalculation;

import com.sap.sailing.domain.tracking.Wind;

public class MockedPolarSheetDeliverer {
    public static double reaching = 5.1;
    public static double up=4.9;
    public static double down=5.3;
    
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
