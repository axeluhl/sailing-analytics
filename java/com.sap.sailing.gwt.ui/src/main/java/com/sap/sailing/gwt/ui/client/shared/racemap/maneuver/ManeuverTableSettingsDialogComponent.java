package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.gwt.ui.client.ManeuverTypeFormatter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class ManeuverTableSettingsDialogComponent implements SettingsDialogComponent<ManeuverTableSettings> {
    
    private final Map<ManeuverType, CheckBox> maneuverType = new HashMap<>();
    private final ManeuverTableSettings initialSettings;
    private final StringMessages stringMessages;
    
    public ManeuverTableSettingsDialogComponent(ManeuverTableSettings initialSettings, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        this.initialSettings = initialSettings;
    }

    @Override 
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        final VerticalPanel contentPanel = new VerticalPanel();
        final Label label = dialog.createHeadlineLabel(stringMessages.maneuverTypesToShowWhenCompetitorIsClicked());
        label.getElement().getStyle().clearPaddingTop();
        contentPanel.add(label);
        for (ManeuverType maneuvertype : ManeuverType.values()) {
            CheckBox checkbox = dialog.createCheckbox(ManeuverTypeFormatter.format(maneuvertype, stringMessages));
            checkbox.setValue(initialSettings.getSelectedManeuverTypes().contains(maneuvertype));
            contentPanel.add(checkbox);
            maneuverType.put(maneuvertype, checkbox);
        }
        return contentPanel;
    }

    @Override
    public ManeuverTableSettings getResult() {
        final Set<ManeuverType> selectedManeuverTypes = new HashSet<ManeuverType>();
        for (Map.Entry<ManeuverType, CheckBox> entry : maneuverType.entrySet()) {
            if (entry.getValue().getValue()) {
                selectedManeuverTypes.add(entry.getKey());
            }
        }
        return new ManeuverTableSettings(selectedManeuverTypes);
    }

    @Override
    public Validator<ManeuverTableSettings> getValidator() {
        return null;
    }

    @Override
    public FocusWidget getFocusWidget() {
        return maneuverType.values().iterator().next();
    }

}
