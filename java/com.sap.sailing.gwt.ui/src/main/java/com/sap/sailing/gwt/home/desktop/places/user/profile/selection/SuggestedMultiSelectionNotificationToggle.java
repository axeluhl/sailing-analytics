package com.sap.sailing.gwt.home.desktop.places.user.profile.selection;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.common.client.SharedResources;

class SuggestedMultiSelectionNotificationToggle extends Composite implements HasEnabled {

    private static LocalUiBinder uiBinder = GWT.create(LocalUiBinder.class);

    interface LocalUiBinder extends UiBinder<Widget, SuggestedMultiSelectionNotificationToggle> {
    }
    
    @UiField SharedResources res;
    @UiField Button toggleButtonUi;
    private boolean enabled = false;
    
    SuggestedMultiSelectionNotificationToggle() {
        initWidget(uiBinder.createAndBindUi(this));
        this.updateUiState(); // TODO remove
    }
    
    @UiHandler("toggleButtonUi")
    void onToggleButtonClicked(ClickEvent event) {
        setEnabled(!isEnabled());
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.updateUiState();
    }
    
    private void updateUiState() {
        toggleButtonUi.setStyleName(res.mainCss().buttonred(), !enabled);
        toggleButtonUi.setText(enabled ? "!Enabled!" : "!Disabled!");
    }
    
}
