package com.sap.sailing.datamining.test.function.test_classes;

import com.sap.sse.datamining.annotations.Dimension;

/*
 * DON'T CHANGE THE METHOD/CLASS NAMES!
 * The tests will fail, because they are reflected via constant strings.
 */

public interface DataTypeWithContext extends DataTypeInterface {
    
    @Dimension("regattaName")
    public String getRegattaName();
    
    @Dimension("raceName")
    public String getRaceName();
    
    @Dimension("legNumber")
    public int getLegNumber();

}
