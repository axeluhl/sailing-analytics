package com.sap.sse.security.ui.client.premium;

import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.security.shared.HasPermissions.Action;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public abstract class PremiumCheckBox extends Composite {

    private final CheckBox checkBox;
    private final Image image;
    private final HorizontalPanel layoutPanel;
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
        final StringMessages stringMessages = StringMessages.INSTANCE;
        this.action = action;
        this.payWallResolver = payWallResolver;
        this.wrapperPanel = new FocusPanel();
        this.layoutPanel = new HorizontalPanel();
        this.wrapperPanel.add(layoutPanel);
        this.checkBox = new CheckBox(label);
        if(!payWallResolver.hasPermission(action)) {
            wrapperPanel.addClickHandler(clickEvent -> new FeatureOverviewDialog(payWallResolver, stringMessages).show());
            layoutPanel.getElement().getStyle().setCursor(Cursor.POINTER);
            checkBox.setEnabled(false);
            image = createPremiumIcon();
            layoutPanel.add(image);
            //FIXME: See bug5593 - This message should contain the plan, which would provide the needed access (if the access is not otherwise blocked)
            layoutPanel.setTitle(stringMessages.unlockWithSubscription());
            image.setWidth("1em");
            image.setHeight("1em");
        }else {
            //TODO: Might want to use an "unlocked" image.
            image = null;
            checkBox.setEnabled(true);
        }
        layoutPanel.add(checkBox);
        initWidget(wrapperPanel);
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
