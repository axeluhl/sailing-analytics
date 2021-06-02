package com.sap.sse.security.ui.client.premium;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.ui.client.UserService;

public abstract class PremiumCheckBox extends Composite {

    private CheckBox checkBox;
    private Image image;
    private HorizontalPanel mainPanel;

    /**
     * A Composite component, that includes a checkbox and an additional premium icon, 
     * indicating that the feature to be enabled is a premium feature if the user does not have the permission.
     * @param label
     * @param userService
     * @param permission
     * @param ownership
     * @param acl
     */
    public PremiumCheckBox(String label, UserService userService, WildcardPermission permission, OwnershipDTO ownership,
            AccessControlListDTO acl) {
        mainPanel = new HorizontalPanel();
        this.checkBox = new CheckBox(label);
        if(!userService.hasPermission(permission, ownership, acl)) {
            checkBox.setEnabled(false);
            this.image = createPremiumIcon();
            mainPanel.add(image);
        }
        mainPanel.add(checkBox);
        initWidget(mainPanel);
    }

    /**
     * This Method can be overridden by Subclasses to accommodate for application specific premium icons.
     * @return Premium Icon
     */
    protected Image createPremiumIcon() {
        return new Image(PremiumIconRessource.INSTANCE.premiumIcon().getSafeUri());
    }
}
