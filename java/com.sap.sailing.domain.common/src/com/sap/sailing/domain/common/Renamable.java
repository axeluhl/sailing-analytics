package com.sap.sailing.domain.common;

import com.sap.sse.common.Named;

public interface Renamable extends Named {
    void setName(String newName);
}
