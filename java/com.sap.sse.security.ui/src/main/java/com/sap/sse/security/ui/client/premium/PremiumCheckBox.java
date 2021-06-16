package com.sap.sse.security.ui.client.premium;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.AccessControlListDTO;
import com.sap.sse.security.shared.dto.OwnershipDTO;
import com.sap.sse.security.ui.client.UserService;

public abstract class PremiumCheckBox extends Composite {

    private final CheckBox checkBox;
    private final Image image;
    private final HorizontalPanel mainPanel;
    private final FocusPanel wrapperPanel;
    private final UserService userService;
    private final WildcardPermission permission;
    private final OwnershipDTO ownership;
    private final AccessControlListDTO acl;

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
        this.userService = userService;
        this.permission = permission;
        this.ownership = ownership;
        this.acl = acl;
        wrapperPanel = new FocusPanel();
        mainPanel = new HorizontalPanel();
        wrapperPanel.add(mainPanel);
        this.checkBox = new CheckBox(label);
        if(!userService.hasPermission(permission, ownership, acl)) {
//            wrapperPanel.addClickHandler(clickEvent -> new FeatureOverviewDialog(null, null));
            checkBox.setEnabled(false);
            this.image = createPremiumIcon();
            mainPanel.add(image);
        }else {
            //TODO: Might want to use an "unlocked" image.
            image = null;
        }
        mainPanel.add(checkBox);
        initWidget(mainPanel);
    }
    
    public PremiumCheckBox(String label, UserService userService, WildcardPermission permission, OwnershipDTO ownership,
            AccessControlListDTO acl, DataEntryDialog<?> dialog) {
        this(label, userService, permission, ownership, acl);
        dialog.ensureHasValueIsValidated(checkBox);
        dialog.ensureFocusWidgetIsLinkedToKeyStrokes(checkBox);
    }

    /**
     * This Method can be overridden by Subclasses to accommodate for application specific premium icons.
     * @return Premium Icon
     */
    protected Image createPremiumIcon() {
        return new Image(PremiumIconRessource.INSTANCE.premiumIcon().getSafeUri());
    }
    
    public FocusPanel getFocusWidget() {
        return this.wrapperPanel;
    }
    
    public CheckBox getCheckBox() {
        return this.checkBox;
    }
    
    public void setValueifUserHasPermission(boolean value) {
        if(userService.hasPermission(permission, ownership, acl)) {
            this.checkBox.setValue(value);
        }
    }
    
    public Boolean getValue() {
        return checkBox.getValue();
    }

    public void setEnabledIfUserHasPermission(Boolean value) {
        if(userService.hasPermission(permission, ownership, acl)) {
            this.checkBox.setEnabled(value);
        }
    }
}
