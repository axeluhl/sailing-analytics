package com.sap.sailing.domain.coursetemplate;

import com.sap.sse.common.Util.Pair;

public interface WithOptionalRepeatablePart {
    boolean hasRepeatablePart();
    
    Pair<Integer, Integer> getRepeatablePart();
}
