package com.sap.sailing.landscape.ui.shared;

import java.util.Optional;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.sap.sse.common.Util;

public interface RedirectDTO extends IsSerializable {
    String getPath();

    default Optional<String> getQuery() {
        return Optional.of("#{query}");
    }
    
    static String toString(String path, Optional<String> query) {
        return path+query.map(q->!Util.hasLength(q)?"":("?"+q)).orElse("");
    }
}
