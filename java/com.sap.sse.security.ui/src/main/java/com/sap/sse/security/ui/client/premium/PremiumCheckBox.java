package com.sap.sse.security.ui.client.premium;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.HasPermissions.Action;

public abstract class PremiumCheckBox extends Composite {

    private final CheckBox checkBox;
    private final Image image;
    private final HorizontalPanel mainPanel;
    private final FocusPanel wrapperPanel;
    private final Action action;
    private final PayWallResolver payWallResolver;

    /**
     * A Composite component, that includes a checkbox and an additional premium icon, 
     * indicating that the feature to be enabled is a premium feature if the user does not have the permission.
     * @param label
     * @param userService
     * @param permission
     * @param ownership
     * @param acl
     */
    public PremiumCheckBox(String label, Action action, PayWallResolver payWallResolver) {
        this.action = action;
        this.payWallResolver = payWallResolver;
        this.wrapperPanel = new FocusPanel();
        this.mainPanel = new HorizontalPanel();
        this.wrapperPanel.add(mainPanel);
        this.checkBox = new CheckBox(label);
        if(!payWallResolver.hasPermission(action)) {
            wrapperPanel.addClickHandler(clickEvent -> new FeatureOverviewDialog(payWallResolver));
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
    
    public PremiumCheckBox(String label, Action action, PayWallResolver payWallResolver, DataEntryDialog<?> dialog) {
        this(label, action, payWallResolver);
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
        if(payWallResolver.hasPermission(action)) {
            this.checkBox.setValue(value);
        }
    }
    
    public Boolean getValue() {
        return checkBox.getValue();
    }

    public void setEnabledIfUserHasPermission(Boolean value) {
        if(payWallResolver.hasPermission(action)) {
            this.checkBox.setEnabled(value);
        }
    }
}
