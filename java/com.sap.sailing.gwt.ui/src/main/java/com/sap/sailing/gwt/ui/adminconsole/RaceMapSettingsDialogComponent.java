package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.ManeuverType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.adminconsole.RaceMapZoomSettings.ZoomTypes;
import com.sap.sailing.gwt.ui.client.DataEntryDialog;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.Validator;
import com.sap.sailing.gwt.ui.client.ManeuverTypeFormatter;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class RaceMapSettingsDialogComponent implements SettingsDialogComponent<RaceMapSettings> {
    //Initializing the lists to prevent a null pointer exception in the first validation call
    private List<Pair<CheckBox, ManeuverType>> checkboxAndManeuverType = new ArrayList<Pair<CheckBox, ManeuverType>>();
    private List<Pair<CheckBox, ZoomTypes>> checkboxAndZoomType = new ArrayList<Pair<CheckBox,ZoomTypes>>();
    private CheckBox zoomOnlyToSelectedCompetitors = new CheckBox();
    private CheckBox checkBoxDouglasPeuckerPoints = new CheckBox();
    private CheckBox showOnlySelectedCompetitors = new CheckBox();
    private LongBox tailLengthBox = new LongBox();
    
    private final StringMessages stringMessages;
    private final RaceMapSettings initialSettings;

    private ArrayList<CheckBox> disableOnlySelectedWhenAreFalse;
    
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
        
        zoomOnlyToSelectedCompetitors = dialog.createCheckbox(stringMessages.autoZoomSelectedCompetitors());//new CheckBox(stringMessages.autoZoomSelectedCompetitors());
        zoomOnlyToSelectedCompetitors.setValue(initialSettings.getZoomSettings().isZoomToSelectedCompetitors());

        HorizontalPanel labelAndZoomSettingsPanel = new HorizontalPanel();
        Label zoomSettingsLabel = new Label(stringMessages.autoZoomTo() + ": ");
        labelAndZoomSettingsPanel.add(zoomSettingsLabel);
        VerticalPanel zoomSettingsBoxesPanel = new VerticalPanel();
        disableOnlySelectedWhenAreFalse = new ArrayList<CheckBox>();
        for (ZoomTypes zoomType : ZoomTypes.values()) {
            if (zoomType != ZoomTypes.NONE) {
                CheckBox cb = dialog.createCheckbox(ZoomTypeFormatter.format(zoomType, stringMessages));
                cb.setValue(initialSettings.getZoomSettings().getTypesToConsiderOnZoom().contains(zoomType), true);
                checkboxAndZoomType.add(new Pair<CheckBox, ZoomTypes>(cb, zoomType));
                zoomSettingsBoxesPanel.add(cb);
                
                //Save specific checkboxes for easier value change handling
                if (zoomType == ZoomTypes.BOATS || zoomType == ZoomTypes.TAILS) {
                    disableOnlySelectedWhenAreFalse.add(cb);
                    cb.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                        @Override
                        public void onValueChange(ValueChangeEvent<Boolean> event) {
                            zoomSettingsChanged();
                        }
                    });
                }
            }
        }
        labelAndZoomSettingsPanel.add(zoomSettingsBoxesPanel);
        vp.add(labelAndZoomSettingsPanel);
        vp.add(zoomOnlyToSelectedCompetitors);
        //Run zoomSettingsChanged to set the checkboxes to their correct state
        zoomSettingsChanged();
        
        showOnlySelectedCompetitors = dialog.createCheckbox(stringMessages.showOnlySelected());
        showOnlySelectedCompetitors.setValue(initialSettings.isShowOnlySelectedCompetitors());
        vp.add(showOnlySelectedCompetitors);
        vp.add(new Label(stringMessages.maneuverTypes()));
        for (ManeuverType maneuverType : ManeuverType.values()) {
            CheckBox checkbox = dialog.createCheckbox(ManeuverTypeFormatter.format(maneuverType, stringMessages));
            checkbox.setValue(initialSettings.isShowManeuverType(maneuverType));
            checkboxAndManeuverType.add(new Pair<CheckBox, ManeuverType>(checkbox, maneuverType));
            vp.add(checkbox);
        }
        checkBoxDouglasPeuckerPoints = dialog.createCheckbox(stringMessages.douglasPeuckerPoints());
        checkBoxDouglasPeuckerPoints.setValue(initialSettings.isShowDouglasPeuckerPoints());
        vp.add(checkBoxDouglasPeuckerPoints);
        return vp;
    }
    
    private void zoomSettingsChanged() {
        boolean disableOnlySelected = true;
        for (Pair<CheckBox, ZoomTypes> pair : checkboxAndZoomType) {
            pair.getA().setEnabled(true);
            if (disableOnlySelectedWhenAreFalse.contains(pair.getA())) {
                if (pair.getA().getValue()) {
                    disableOnlySelected = false;
                }
            }
        }

        zoomOnlyToSelectedCompetitors.setEnabled(!disableOnlySelected);
        if (disableOnlySelected) {
            zoomOnlyToSelectedCompetitors.setValue(false);
        }
    }

    @Override
    public RaceMapSettings getResult() {
        RaceMapSettings result = new RaceMapSettings();
        for (Pair<CheckBox, ManeuverType> p : checkboxAndManeuverType) {
            result.showManeuverType(p.getB(), p.getA().getValue());
        }
        result.setZoomSettings(getZoomSettings());
        result.setShowDouglasPeuckerPoints(checkBoxDouglasPeuckerPoints.getValue());
        result.setShowOnlySelectedCompetitors(showOnlySelectedCompetitors.getValue());
        result.setTailLengthInMilliseconds(tailLengthBox.getValue() == null ? -1 : tailLengthBox.getValue()*1000l);
        return result;
    }
    
    private RaceMapZoomSettings getZoomSettings() {
        ArrayList<ZoomTypes> zoomTypes = new ArrayList<ZoomTypes>();
        boolean noAutoZoomSelected = true;
        for (Pair<CheckBox, ZoomTypes> pair : checkboxAndZoomType) {
            if (pair.getA().getValue()) {
                zoomTypes.add(pair.getB());
                noAutoZoomSelected = false;
            }
        }
        if (noAutoZoomSelected) {
            zoomTypes.add(ZoomTypes.NONE);
        }
        return new RaceMapZoomSettings(zoomTypes, zoomOnlyToSelectedCompetitors.getValue());
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
