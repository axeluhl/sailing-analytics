package com.sap.sse.gwt.adminconsole;

import com.sap.sse.common.impl.NamedImpl;
import com.sap.sse.security.shared.Permission;

/**
 * Specifies a feature with a name and the role names, one of which is required in order to be allowed
 * to see and use the feature.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class AdminConsoleFeatures extends NamedImpl {
    private static final long serialVersionUID = 7441608351911944793L;
    private Permission requiredPermission;
    
    public AdminConsoleFeatures(String name, Permission requiredPermission) {
        super(name);
        this.requiredPermission = requiredPermission;
    }

    /**
     * If the user has at least one of these roles, he/she is permitted to use this feature.
     */
    public Permission getRequiredPermission() {
        return requiredPermission;
    }
}
