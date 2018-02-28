package com.sap.sailing.gwt.ui.client.shared.racemap.maneuver;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
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
    private final ManeuverTableSettings initialSettings;
    private final Map<ManeuverType, CheckBox> maneuverType;

    private final StringMessages stringMessages;
    
    public ManeuverTableSettingsDialogComponent(ManeuverTableSettings initialSettings, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        this.initialSettings = initialSettings;
        maneuverType = new LinkedHashMap<>();
    }

    @Override 
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();

        Label createHeadlineLabel = dialog.createHeadlineLabel(stringMessages.maneuverTypesToShowWhenCompetitorIsClicked());
        vp.add(createHeadlineLabel);

        Grid grid = new Grid(1,2);
        grid.setCellPadding(5);
        vp.add(grid);
        
        VerticalPanel windDirectionSourcesPanel = new VerticalPanel();
        grid.setWidget(0, 0, windDirectionSourcesPanel);

        VerticalPanel windSpeedSourcesPanel = new VerticalPanel();
        grid.setWidget(0, 1, windSpeedSourcesPanel);

        for (ManeuverType maneuvertype : ManeuverType.values()) {
            CheckBox checkbox = dialog.createCheckbox(ManeuverTypeFormatter.format(maneuvertype, stringMessages));
            //TODO enable disable handing if some are restricted
            maneuverType.put(maneuvertype, checkbox);
            checkbox.setValue(initialSettings.getSelectedManeuverTypes().contains(maneuvertype));
            checkbox.getElement().getStyle().setMarginLeft(15.0, Unit.PX);
            windDirectionSourcesPanel.add(checkbox);
        }
        return vp;
    }

    @Override
    public ManeuverTableSettings getResult() {
        Set<ManeuverType> selectedManeuverTypes = new HashSet<ManeuverType>();
        for (Map.Entry<ManeuverType, CheckBox> e : maneuverType.entrySet()) {
            if (e.getValue().getValue()) {
                selectedManeuverTypes.add(e.getKey());
            }
        }
        return new ManeuverTableSettings(selectedManeuverTypes);
    }

    @Override
    public Validator<ManeuverTableSettings> getValidator() {
        return new Validator<ManeuverTableSettings>() {
            @Override
            public String getErrorMessage(ManeuverTableSettings valueToValidate) {
                String errorMessage = null;
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return maneuverType.values().iterator().next();
    }

}
