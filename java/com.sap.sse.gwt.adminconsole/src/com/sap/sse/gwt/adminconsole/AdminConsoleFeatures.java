package com.sap.sse.gwt.adminconsole;

import com.sap.sse.common.impl.NamedImpl;

/**
 * Specifies a feature with a name and the role names, one of which is required in order to be allowed
 * to see and use the feature.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class AdminConsoleFeatures extends NamedImpl {
    private static final long serialVersionUID = 7441608351911944793L;
    private String[] enabledRoles;
    
    public AdminConsoleFeatures(String name, String... enabledRoles) {
        super(name);
        this.enabledRoles = enabledRoles;
    }

    /**
     * If the user has at least one of these roles, he/she is permitted to use this feature.
     */
    public String[] getEnabledRoles() {
        return enabledRoles;
    }
}
