package com.sap.sailing.domain.test.markpassing;

import com.sap.sailing.domain.tracking.Wind;

public interface PolarSheetDeliverer {
    
    double getReaching(Wind w);
    
    double getUpwind(Wind w);
     
    double getDownwind(Wind w);
    

}
