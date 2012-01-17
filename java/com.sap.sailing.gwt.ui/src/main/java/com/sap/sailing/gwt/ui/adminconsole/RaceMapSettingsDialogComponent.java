package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.ManeuverTypeFormatter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class RaceMapSettingsDialogComponent implements SettingsDialogComponent<RaceMapSettings> {
    private List<Pair<CheckBox, ManeuverType>> checkboxAndType;
    private CheckBox checkBoxDouglasPeuckerPoints;
    private CheckBox showOnlySelectedCompetitors;
    private LongBox tailLengthBox;
    private final StringMessages stringMessages;
    private final RaceMapSettings initialSettings;
    
    public RaceMapSettingsDialogComponent(RaceMapSettings settings, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        initialSettings = settings;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<RaceMapSettings> dialog) {
        VerticalPanel vp = new VerticalPanel();
        HorizontalPanel labelAndTailLengthBoxPanel = new HorizontalPanel();
        labelAndTailLengthBoxPanel.add(new Label(stringMessages.tailLength()));
        tailLengthBox = dialog.createLongBox((int) (initialSettings.getTailLengthInMilliseconds() / 1000), 4);
        labelAndTailLengthBoxPanel.add(tailLengthBox);
        vp.add(labelAndTailLengthBoxPanel);
        showOnlySelectedCompetitors = dialog.createCheckbox(stringMessages.showOnlySelected());
        showOnlySelectedCompetitors.setValue(initialSettings.isShowOnlySelectedCompetitors());
        vp.add(showOnlySelectedCompetitors);
        vp.add(new Label(stringMessages.maneuverTypes()));
        checkboxAndType = new ArrayList<Pair<CheckBox, ManeuverType>>();
        for (ManeuverType maneuverType : ManeuverType.values()) {
            CheckBox checkbox = dialog.createCheckbox(ManeuverTypeFormatter.format(maneuverType, stringMessages));
            checkbox.setValue(initialSettings.isShowManeuverType(maneuverType));
            checkboxAndType.add(new Pair<CheckBox, ManeuverType>(checkbox, maneuverType));
            vp.add(checkbox);
        }
        checkBoxDouglasPeuckerPoints = dialog.createCheckbox(stringMessages.douglasPeuckerPoints());
        checkBoxDouglasPeuckerPoints.setValue(initialSettings.isShowDouglasPeuckerPoints());
        vp.add(checkBoxDouglasPeuckerPoints);
        return vp;
    }

    @Override
    public RaceMapSettings getResult() {
        RaceMapSettings result = new RaceMapSettings();
        for (Pair<CheckBox, ManeuverType> p : checkboxAndType) {
            result.showManeuverType(p.getB(), p.getA().getValue());
        }
        result.setShowDouglasPeuckerPoints(checkBoxDouglasPeuckerPoints.getValue());
        result.setShowOnlySelectedCompetitors(showOnlySelectedCompetitors.getValue());
        result.setTailLengthInMilliseconds(tailLengthBox.getValue() == null ? -1 : tailLengthBox.getValue()*1000l);
        return result;
    }

    @Override
    public Validator<RaceMapSettings> getValidator() {
        return new Validator<RaceMapSettings>() {
            @Override
            public String getErrorMessage(RaceMapSettings valueToValidate) {
                String errorMessage = null;
                if (valueToValidate.getTailLengthInMilliseconds() < 0) {
                    errorMessage = stringMessages.tailLengthMustBeNonNegative();
                }
                return errorMessage;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return tailLengthBox;
    }
}
