package com.sap.sailing.selenium.pages.gwt.query;

import com.sap.sailing.selenium.pages.gwt.query.path.BooleanPath;
import com.sap.sailing.selenium.pages.gwt.query.path.StringPath;

public class PathFactory {
    
    public static Path<?> createPath(Class<?> type, PathMetadata<?> metadata) {
        if (String.class.equals(type)) {
            return new StringPath(metadata);
        }
        
        if (Boolean.class.equals(type) || boolean.class.equals(type)) {
            return new BooleanPath(metadata);
        }
        
        return null;
    }
}
