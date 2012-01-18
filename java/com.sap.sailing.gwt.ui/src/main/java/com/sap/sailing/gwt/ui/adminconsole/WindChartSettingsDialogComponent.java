package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class WindChartSettingsDialogComponent implements SettingsDialogComponent<WindChartSettings> {
    private final WindChartSettings initialSettings;
    private final Map<WindSource, CheckBox> checkboxes;
    
    public WindChartSettingsDialogComponent(WindChartSettings initialSettings) {
        super();
        this.initialSettings = initialSettings;
        checkboxes = new HashMap<WindSource, CheckBox>();
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<WindChartSettings> dialog) {
        VerticalPanel vp = new VerticalPanel();
        for (WindSource windSource : WindSource.values()) {
            CheckBox checkbox = dialog.createCheckbox(windSource.name());
            checkboxes.put(windSource, checkbox);
            checkbox.setValue(initialSettings.getWindSourcesToDisplay().contains(windSource));
            vp.add(checkbox);
        }
        return vp;
    }

    @Override
    public WindChartSettings getResult() {
        Set<WindSource> windSourcesToDisplay = new HashSet<WindSource>();
        for (Map.Entry<WindSource, CheckBox> e : checkboxes.entrySet()) {
            if (e.getValue().getValue()) {
                windSourcesToDisplay.add(e.getKey());
            }
        }
        return new WindChartSettings(windSourcesToDisplay);
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
