package com.sap.sailing.gwt.ui.adminconsole;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class WindChartSettingsDialogComponent implements SettingsDialogComponent<WindChartSettings> {
    private final WindChartSettings initialSettings;
    private IntegerBox resolutionInSecondsBox;
    private final Map<WindSourceType, CheckBox> checkboxes;
    private final StringMessages stringMessages;
    
    public WindChartSettingsDialogComponent(WindChartSettings initialSettings, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        this.initialSettings = initialSettings;
        checkboxes = new LinkedHashMap<WindSourceType, CheckBox>();
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();
        resolutionInSecondsBox = dialog.createIntegerBox((int) (initialSettings.getResolutionInMilliseconds()/1000), /* visibleLength */ 5);
        HorizontalPanel hp = new HorizontalPanel();
        hp.add(new Label(stringMessages.stepSizeInSeconds()));
        hp.add(resolutionInSecondsBox);
        vp.add(hp);
        for (WindSourceType windSourceType : WindSourceType.values()) {
            CheckBox checkbox = dialog.createCheckbox(WindSourceTypeFormatter.format(windSourceType, stringMessages));
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
        return new WindChartSettings(windSourceTypesToDisplay, resolutionInSecondsBox.getValue() == null ? -1 : resolutionInSecondsBox.getValue()*1000);
    }

    @Override
    public Validator<WindChartSettings> getValidator() {
        return new Validator<WindChartSettings>() {
            @Override
            public String getErrorMessage(WindChartSettings valueToValidate) {
                String errorMessage = null;
                if (valueToValidate.getResolutionInMilliseconds() < 1) {
                    errorMessage = stringMessages.stepSizeMustBeGreaterThanNull();
                }
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return resolutionInSecondsBox;
    }

}
