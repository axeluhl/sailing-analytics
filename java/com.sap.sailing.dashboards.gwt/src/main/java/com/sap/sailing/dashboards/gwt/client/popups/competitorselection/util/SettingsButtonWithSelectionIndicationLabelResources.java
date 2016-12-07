package com.sap.sailing.dashboards.gwt.client.popups.competitorselection.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * @author Alexander Ries (D062114)
 *
 */
public interface SettingsButtonWithSelectionIndicationLabelResources extends ClientBundle {

    public static final SettingsButtonWithSelectionIndicationLabelResources INSTANCE =  GWT.create(SettingsButtonWithSelectionIndicationLabelResources.class);

    @Source("com/sap/sailing/dashboards/gwt/client/images/settings.png")
    ImageResource settings();
    
    @Source("com/sap/sailing/dashboards/gwt/client/images/settings_disabled.png")
    ImageResource settingsDisabled();
    
    @Source({"com/sap/sailing/dashboards/gwt/client/theme/theme.gss", "SettingsButtonWithSelectionIndicationLabel.gss"})
    StartlineAdvantagesOnLineChartGSS gss();

    public interface StartlineAdvantagesOnLineChartGSS extends CssResource {
        String settings_button();
        String settings_button_image();
        String settings_indication_label();
        String settings_indication_label_disabled();
    }
}
