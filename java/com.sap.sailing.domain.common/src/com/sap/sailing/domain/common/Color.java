package com.sap.sailing.domain.common;

import java.io.Serializable;
import com.sap.sailing.domain.common.impl.Util;

public interface Color extends Serializable {
    Util.Triple<Integer, Integer, Integer> getAsRGB();

    String getAsHtml();
}
