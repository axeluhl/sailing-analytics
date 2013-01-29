package com.sap.sailing.domain.common;

import java.util.HashMap;


/**
 * <p>An object can undergo different states during its lifecycle. This interface is the base
 * for a definition of these states. </p>
 * 
 * @author Simon Pamies (info@pamies.de)
 * @since Jan 28, 2013
 */
public interface LifecycleState {

    /**
     * @return Named property value or null if there is no such property
     */
    public Object getProperty(String name);
    
    /**
     * Updates the given property
     * 
     * @param name Name of the property
     * @param value
     */
    public void updateProperty(String name, Object value);

    /**
     * @return All properties contained
     */
    public HashMap<String, Object> allProperties();
}
