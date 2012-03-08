package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class WindChartSettingsDialogComponent implements SettingsDialogComponent<WindChartSettings> {
    private final WindChartSettings initialSettings;
    private final Map<WindSourceType, CheckBox> checkboxes;
    
    public WindChartSettingsDialogComponent(WindChartSettings initialSettings) {
        super();
        this.initialSettings = initialSettings;
        checkboxes = new LinkedHashMap<WindSourceType, CheckBox>();
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();
        for (WindSourceType windSourceType : WindSourceType.values()) {
            CheckBox checkbox = dialog.createCheckbox(windSourceType.name());
            checkboxes.put(windSourceType, checkbox);
            checkbox.setValue(initialSettings.getWindSourceTypesToDisplay().contains(windSourceType));
            vp.add(checkbox);
        }
        return vp;
    }

    @Override
    public WindChartSettings getResult() {
        Set<WindSourceType> windSourceTypesToDisplay = new HashSet<WindSourceType>();
        for (Map.Entry<WindSourceType, CheckBox> e : checkboxes.entrySet()) {
            if (e.getValue().getValue()) {
                windSourceTypesToDisplay.add(e.getKey());
            }
        }
        return new WindChartSettings(windSourceTypesToDisplay);
    }

    @Override
    public Validator<WindChartSettings> getValidator() {
        // with checkboxes only, nothing can go wrong :-)
        return null;
    }

    @Override
    public FocusWidget getFocusWidget() {
        return checkboxes.values().iterator().next();
    }

}
