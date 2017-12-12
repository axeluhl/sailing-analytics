package com.sap.sse.security.ui.client.component;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sse.security.shared.Role;
import com.sap.sse.security.ui.client.UserManagementServiceAsync;
import com.sap.sse.security.ui.client.i18n.StringMessages;

/**
 * Displays and allows users to edit {@link Role}s. This includes creating and removing
 * roles as well as changing the sets of permissions implied by a role.<p>
 * 
 * The panel is <em>not</em> a standalone top-level AdminConsole panel but just a widget
 * that can be included, e.g., in a user management panel.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class RolesPanel extends FlowPanel {
    private final Button addButton;
    private final Button deleteButton;
    private final Button refreshButton;
    private final FlushableSortedCellTableWithStylableHeaders rolesTable;
    
    public RolesPanel(StringMessages stringMessages, UserManagementServiceAsync userManagementService) {
        
    }
}
