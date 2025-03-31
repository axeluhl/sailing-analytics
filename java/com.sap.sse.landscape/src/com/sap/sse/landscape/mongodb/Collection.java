package com.sap.sse.landscape.mongodb;

import com.sap.sse.common.Named;

public interface Collection extends Named {
    String getMD5Hash();
    
    /**
     * Drops this collection with all its content. Use with care!
     */
    void drop();
}
