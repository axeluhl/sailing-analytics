package com.sap.sailing.domain.coursetemplate;

import java.util.Collections;

public interface HasTags {
    
    default Iterable<String> getTags() {
        // FIXME implement tagging feature
        return Collections.emptySet();
    }
}
