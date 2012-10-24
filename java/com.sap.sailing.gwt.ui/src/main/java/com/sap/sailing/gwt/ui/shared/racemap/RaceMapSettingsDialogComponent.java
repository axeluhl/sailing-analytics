package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
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
import com.sap.sailing.gwt.ui.shared.racemap.RaceMapHelpLinesSettings.HelpLineTypes;
import com.sap.sailing.gwt.ui.shared.racemap.RaceMapZoomSettings.ZoomTypes;

public class RaceMapSettingsDialogComponent implements SettingsDialogComponent<RaceMapSettings> {
    //Initializing the lists to prevent a null pointer exception in the first validation call
    private List<Pair<CheckBox, ManeuverType>> checkboxAndManeuverType = new ArrayList<Pair<CheckBox, ManeuverType>>();
    private List<Pair<CheckBox, ZoomTypes>> checkboxAndZoomType = new ArrayList<Pair<CheckBox,ZoomTypes>>();
    private List<Pair<CheckBox, HelpLineTypes>> checkboxAndHelpLineType = new ArrayList<Pair<CheckBox,HelpLineTypes>>();
    private CheckBox zoomOnlyToSelectedCompetitorsCheckBox;
    private CheckBox showDouglasPeuckerPointsCheckBox;
    private CheckBox showOnlySelectedCompetitorsCheckBox;
    private CheckBox showTailsCheckBox;
    private CheckBox showAllCompetitorsCheckBox;
    private CheckBox showSelectedCompetitorsInfoCheckBox;
    private LongBox tailLengthBox;
//    private DoubleBox buoyZoneRadiusBox;
    private IntegerBox maxVisibleCompetitorsCountBox;
    
    private final StringMessages stringMessages;
    private final RaceMapSettings initialSettings;

    private ArrayList<CheckBox> disableOnlySelectedWhenAreFalse;
    
    public RaceMapSettingsDialogComponent(RaceMapSettings settings, StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        initialSettings = settings;
    }

    @Override
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();

        Label competitorsLabel = dialog.createHeadlineLabel(stringMessages.competitors());
        vp.add(competitorsLabel);

        // max competitors count settings
        HorizontalPanel maxCompetitotorsCountPanel = new HorizontalPanel();
        showAllCompetitorsCheckBox = dialog.createCheckbox(stringMessages.showAllCompetitors());
        showAllCompetitorsCheckBox.setValue(initialSettings.isShowAllCompetitors());
        maxCompetitotorsCountPanel.add(showAllCompetitorsCheckBox);
        showAllCompetitorsCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> vce) {
                boolean newValue = vce.getValue();
                RaceMapSettingsDialogComponent.this.maxVisibleCompetitorsCountBox.setEnabled(!newValue);
            }
        });
        Label maxCompetitorsCountLabel = new Label(stringMessages.maximalCount() + ":");
        maxCompetitorsCountLabel.getElement().getStyle().setMarginLeft(25, Unit.PX);
        maxCompetitotorsCountPanel.add(maxCompetitorsCountLabel);
        maxCompetitotorsCountPanel.setCellVerticalAlignment(maxCompetitorsCountLabel, HasVerticalAlignment.ALIGN_MIDDLE);
        maxVisibleCompetitorsCountBox = dialog.createIntegerBox(initialSettings.getMaxVisibleCompetitorsCount(), 4);
        maxVisibleCompetitorsCountBox.setEnabled(!initialSettings.isShowAllCompetitors());
        maxCompetitotorsCountPanel.add(maxVisibleCompetitorsCountBox);
        maxCompetitotorsCountPanel.setCellVerticalAlignment(maxVisibleCompetitorsCountBox, HasVerticalAlignment.ALIGN_MIDDLE);
        vp.add(maxCompetitotorsCountPanel);
        
        showOnlySelectedCompetitorsCheckBox = dialog.createCheckbox(stringMessages.showOnlySelectedCompetitors());
        showOnlySelectedCompetitorsCheckBox.setValue(initialSettings.isShowOnlySelectedCompetitors());
        vp.add(showOnlySelectedCompetitorsCheckBox);

        showSelectedCompetitorsInfoCheckBox = dialog.createCheckbox(stringMessages.showSelectedCompetitorsInfo());
        showSelectedCompetitorsInfoCheckBox.setValue(initialSettings.isShowSelectedCompetitorsInfo());
        vp.add(showSelectedCompetitorsInfoCheckBox);
        
        Label zoomLabel = dialog.createHeadlineLabel(stringMessages.zoom());
        vp.add(zoomLabel);
        
        HorizontalPanel zoomSettingsPanel = new HorizontalPanel();
        Label zoomSettingsLabel = new Label(stringMessages.autoZoomTo() + ": ");
        zoomSettingsPanel.add(zoomSettingsLabel);
        VerticalPanel zoomSettingsBoxesPanel = new VerticalPanel();
        disableOnlySelectedWhenAreFalse = new ArrayList<CheckBox>();
        for (ZoomTypes zoomType : ZoomTypes.values()) {
            if (zoomType != ZoomTypes.NONE) {
                CheckBox cb = dialog.createCheckbox(RaceMapSettingsTypeFormatter.formatZoomType(zoomType, stringMessages));
                cb.setValue(initialSettings.getZoomSettings().getTypesToConsiderOnZoom().contains(zoomType), false);
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
        zoomSettingsPanel.add(zoomSettingsBoxesPanel);
        vp.add(zoomSettingsPanel);
        
        zoomOnlyToSelectedCompetitorsCheckBox = dialog.createCheckbox(stringMessages.autoZoomSelectedCompetitors());
        zoomOnlyToSelectedCompetitorsCheckBox.setValue(initialSettings.getZoomSettings().isZoomToSelectedCompetitors());
        vp.add(zoomOnlyToSelectedCompetitorsCheckBox);
        //Run zoomSettingsChanged to set the checkboxes to their correct state
        zoomSettingsChanged();
        
        Label maneuversLabel = dialog.createHeadlineLabel(stringMessages.maneuverTypesToShowWhenCompetitorIsClicked());
        vp.add(maneuversLabel);
        for (ManeuverType maneuverType : ManeuverType.values()) {
            CheckBox checkbox = dialog.createCheckbox(ManeuverTypeFormatter.format(maneuverType, stringMessages));
            checkbox.setValue(initialSettings.isShowManeuverType(maneuverType));
            checkboxAndManeuverType.add(new Pair<CheckBox, ManeuverType>(checkbox, maneuverType));
            vp.add(checkbox);
        }
        showDouglasPeuckerPointsCheckBox = dialog.createCheckbox(stringMessages.douglasPeuckerPoints());
        showDouglasPeuckerPointsCheckBox.setValue(initialSettings.isShowDouglasPeuckerPoints());
        vp.add(showDouglasPeuckerPointsCheckBox);
        
        Label helpLinesLabel = dialog.createHeadlineLabel(stringMessages.helpLines());
        vp.add(helpLinesLabel);

        // boat tail settings
        HorizontalPanel tailSettingsPanel = new HorizontalPanel();
        showTailsCheckBox = dialog.createCheckbox(stringMessages.boatTails());
        showTailsCheckBox.setValue(initialSettings.isShowTails());
        tailSettingsPanel.add(showTailsCheckBox);
        showTailsCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> vce) {
                boolean newValue = vce.getValue();
                RaceMapSettingsDialogComponent.this.tailLengthBox.setEnabled(newValue);
            }
        });
        Label tailLengthLabel = new Label(stringMessages.lengthInSeconds() + ":");
        tailLengthLabel.getElement().getStyle().setMarginLeft(25, Unit.PX);
        tailSettingsPanel.add(tailLengthLabel);
        tailSettingsPanel.setCellVerticalAlignment(tailLengthLabel, HasVerticalAlignment.ALIGN_MIDDLE);
        tailLengthBox = dialog.createLongBox((int) (initialSettings.getTailLengthInMilliseconds() / 1000), 4);
        tailLengthBox.setEnabled(initialSettings.isShowTails());
        tailSettingsPanel.add(tailLengthBox);
        tailSettingsPanel.setCellVerticalAlignment(tailLengthBox, HasVerticalAlignment.ALIGN_MIDDLE);
        vp.add(tailSettingsPanel);

        VerticalPanel helpLineSettingsBoxesPanel = new VerticalPanel();
        for (HelpLineTypes helpLineType : HelpLineTypes.values()) {
                CheckBox cb = dialog.createCheckbox(RaceMapSettingsTypeFormatter.formatHelpLineType(helpLineType, stringMessages));
                cb.setValue(initialSettings.getHelpLinesSettings().containsHelpLine(helpLineType));
                checkboxAndHelpLineType.add(new Pair<CheckBox, HelpLineTypes>(cb, helpLineType));
                helpLineSettingsBoxesPanel.add(cb);
        }
        vp.add(helpLineSettingsBoxesPanel);
        
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

        zoomOnlyToSelectedCompetitorsCheckBox.setEnabled(!disableOnlySelected);
        if (disableOnlySelected) {
            zoomOnlyToSelectedCompetitorsCheckBox.setValue(false);
        }
    }

    @Override
    public RaceMapSettings getResult() {
        RaceMapSettings result = new RaceMapSettings();
        for (Pair<CheckBox, ManeuverType> p : checkboxAndManeuverType) {
            result.showManeuverType(p.getB(), p.getA().getValue());
        }
        result.setZoomSettings(getZoomSettings());
        result.setHelpLinesSettings(getHelpLinesSettings());
        result.setShowDouglasPeuckerPoints(showDouglasPeuckerPointsCheckBox.getValue());
        result.setShowOnlySelectedCompetitors(showOnlySelectedCompetitorsCheckBox.getValue());
        result.setShowSelectedCompetitorsInfo(showSelectedCompetitorsInfoCheckBox.getValue());
        result.setShowAllCompetitors(showAllCompetitorsCheckBox.getValue());
        result.setShowTails(showTailsCheckBox.getValue());
        result.setTailLengthInMilliseconds(tailLengthBox.getValue() == null ? -1 : tailLengthBox.getValue()*1000l);
        result.setMaxVisibleCompetitorsCount(maxVisibleCompetitorsCountBox.getValue() == null ? -1 : maxVisibleCompetitorsCountBox.getValue());
        
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
        return new RaceMapZoomSettings(zoomTypes, zoomOnlyToSelectedCompetitorsCheckBox.getValue());
    }

    private RaceMapHelpLinesSettings getHelpLinesSettings() {
        Set<HelpLineTypes> helpLineTypes = new HashSet<HelpLineTypes>();
        for (Pair<CheckBox, HelpLineTypes> pair : checkboxAndHelpLineType) {
            if (pair.getA().getValue()) {
                helpLineTypes.add(pair.getB());
            }
        }
        return new RaceMapHelpLinesSettings(helpLineTypes);
    }

    @Override
    public Validator<RaceMapSettings> getValidator() {
        return new Validator<RaceMapSettings>() {
            @Override
            public String getErrorMessage(RaceMapSettings valueToValidate) {
                String errorMessage = null;
                if (valueToValidate.isShowTails() && valueToValidate.getTailLengthInMilliseconds() <= 0) {
                    errorMessage = stringMessages.tailLengthMustBePositive();
                } else if (!valueToValidate.isShowAllCompetitors() && valueToValidate.getMaxVisibleCompetitorsCount() <= 0) {
                    errorMessage = stringMessages.maxVisibleCompetitorsCountMustBePositive();
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
