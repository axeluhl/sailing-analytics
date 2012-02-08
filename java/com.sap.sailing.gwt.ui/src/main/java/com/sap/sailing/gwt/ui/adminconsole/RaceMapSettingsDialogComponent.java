package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.adminconsole.RaceMapSettings.ZoomSettings;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.ManeuverTypeFormatter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class RaceMapSettingsDialogComponent implements SettingsDialogComponent<RaceMapSettings> {
    private List<Pair<CheckBox, ManeuverType>> checkboxAndType;
    private ListBox zoomSettingsBox;
    private CheckBox includeTailsToAutoZoom;
    private CheckBox checkBoxDouglasPeuckerPoints;
    private CheckBox showOnlySelectedCompetitors;
    private LongBox tailLengthBox;
    private final StringMessages stringMessages;
    private final RaceMapSettings initialSettings;
    
    private Map<String, ZoomSettings> zoomSettingsMap;
    
    public RaceMapSettingsDialogComponent(RaceMapSettings settings, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        initialSettings = settings;
        zoomSettingsMap = new HashMap<String, RaceMapSettings.ZoomSettings>();
        zoomSettingsMap.put(stringMessages.autoZoomManual(), ZoomSettings.MANUAL);
        zoomSettingsMap.put(stringMessages.autoZoomToBoats(), ZoomSettings.ZOOM_TO_BOATS);
        zoomSettingsMap.put(stringMessages.autoZoomToBuoys(), ZoomSettings.ZOOM_TO_BUOYS);
        zoomSettingsMap.put(stringMessages.autoZoomToBoatsAndBuoys(), ZoomSettings.ZOOM_TO_BOATS_AND_BUOYS);
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<RaceMapSettings> dialog) {
        VerticalPanel vp = new VerticalPanel();
        HorizontalPanel labelAndTailLengthBoxPanel = new HorizontalPanel();
        labelAndTailLengthBoxPanel.add(new Label(stringMessages.tailLength()));
        tailLengthBox = dialog.createLongBox((int) (initialSettings.getTailLengthInMilliseconds() / 1000), 4);
        labelAndTailLengthBoxPanel.add(tailLengthBox);
        vp.add(labelAndTailLengthBoxPanel);

        HorizontalPanel labelAndZoomSettingsBoxPanel = new HorizontalPanel();
        Label zoomSettingsLabel = new Label(stringMessages.autoZoomTo() + ": ");
        labelAndZoomSettingsBoxPanel.add(zoomSettingsLabel);
        zoomSettingsBox = new ListBox();
        for (String zoomSettingLabel : zoomSettingsMap.keySet()) {
            zoomSettingsBox.addItem(zoomSettingLabel);
        }
        zoomSettingsBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                zoomSettingsSelectionChanged();
            }
        });
        labelAndZoomSettingsBoxPanel.add(zoomSettingsBox);
        vp.add(labelAndZoomSettingsBoxPanel);
        
        includeTailsToAutoZoom = new CheckBox(stringMessages.includeTailsToAutoZoom());
        includeTailsToAutoZoom.setValue(initialSettings.isIncludeTailsToAutoZoom());
        vp.add(includeTailsToAutoZoom);
        //Set selected zoom setting and set state of the includeTailsToAutoZoomBox
        zoomSettingsBox.setSelectedIndex(getIndexOfZoomSettingInBox(initialSettings.getZoomSetting()));
        zoomSettingsSelectionChanged();
        
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
    
    private void zoomSettingsSelectionChanged() {
        ZoomSettings currentZoomSetting = getSelectedZoomSetting();
        if (currentZoomSetting == ZoomSettings.MANUAL || currentZoomSetting == ZoomSettings.ZOOM_TO_BUOYS) {
            includeTailsToAutoZoom.setValue(false);
            includeTailsToAutoZoom.setEnabled(false);
        } else {
            includeTailsToAutoZoom.setEnabled(true);
        }
    }
    
    private ZoomSettings getSelectedZoomSetting() {
        return zoomSettingsMap.get(zoomSettingsBox.getItemText(zoomSettingsBox.getSelectedIndex()));
    }
    
    private int getIndexOfZoomSettingInBox(ZoomSettings zoomSetting) {
        int index = -1;
        for (int i = 0; i < zoomSettingsBox.getItemCount(); i++) {
            if (zoomSettingsMap.get(zoomSettingsBox.getItemText(i)) == zoomSetting) {
                index = i;
                break;
            }
        }
        return index;
    }

    @Override
    public RaceMapSettings getResult() {
        RaceMapSettings result = new RaceMapSettings();
        for (Pair<CheckBox, ManeuverType> p : checkboxAndType) {
            result.showManeuverType(p.getB(), p.getA().getValue());
        }
        result.setZoomSetting(getSelectedZoomSetting());
        result.setIncludeTailsToAutoZoom(includeTailsToAutoZoom.getValue());
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
