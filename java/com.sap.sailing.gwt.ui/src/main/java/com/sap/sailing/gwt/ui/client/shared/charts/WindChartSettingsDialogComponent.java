package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sse.gwt.client.controls.IntegerBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class WindChartSettingsDialogComponent implements SettingsDialogComponent<WindChartSettings> {
    private final WindChartSettings initialSettings;
    private IntegerBox resolutionInSecondsBox;
    private final Map<WindSourceType, CheckBox> windDirectionsCheckboxes;
    private final Map<WindSourceType, CheckBox> windSpeedCheckboxes;
    private CheckBox showWindSpeedSeriesCheckbox;
    private CheckBox showWindDirectionsSeriesCheckbox;

    private final StringMessages stringMessages;
    
    public WindChartSettingsDialogComponent(WindChartSettings initialSettings, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        this.initialSettings = initialSettings;
        windDirectionsCheckboxes = new LinkedHashMap<WindSourceType, CheckBox>();
        windSpeedCheckboxes = new LinkedHashMap<WindSourceType, CheckBox>();
    }

    @Override 
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();
        resolutionInSecondsBox = dialog.createIntegerBox((int) (initialSettings.getResolutionInMilliseconds()/1000), /* visibleLength */ 5);

        HorizontalPanel hp = new HorizontalPanel();
        hp.add(new Label(stringMessages.stepSizeInSeconds() + ":"));
        hp.add(resolutionInSecondsBox);
        vp.add(hp);

        Label createHeadlineLabel = dialog.createHeadlineLabel(stringMessages.windSourcesUsed());
        vp.add(createHeadlineLabel);

        Grid grid = new Grid(1,2);
        grid.setCellPadding(5);
        vp.add(grid);
        
        VerticalPanel windDirectionSourcesPanel = new VerticalPanel();
        grid.setWidget(0, 0, windDirectionSourcesPanel);

        VerticalPanel windSpeedSourcesPanel = new VerticalPanel();
        grid.setWidget(0, 1, windSpeedSourcesPanel);

        showWindSpeedSeriesCheckbox = dialog.createCheckbox(stringMessages.showWindSpeedSeries());
        showWindSpeedSeriesCheckbox.setTitle(stringMessages.showWindSpeedSeriesTooltip());
        showWindSpeedSeriesCheckbox.setValue(initialSettings.isShowWindSpeedSeries());
        showWindSpeedSeriesCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                for (Map.Entry<WindSourceType, CheckBox> box : windSpeedCheckboxes.entrySet()) {
                    box.getValue().setEnabled(event.getValue());
                }
            }
        });
        windSpeedSourcesPanel.add(showWindSpeedSeriesCheckbox);
        showWindDirectionsSeriesCheckbox = dialog.createCheckbox(stringMessages.showWindDirectionSeries());
        showWindDirectionsSeriesCheckbox.setTitle(stringMessages.showWindDirectionSeriesTooltip());
        showWindDirectionsSeriesCheckbox.setValue(initialSettings.isShowWindDirectionsSeries());
        showWindDirectionsSeriesCheckbox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                for (Map.Entry<WindSourceType, CheckBox> box : windDirectionsCheckboxes.entrySet()) {
                    box.getValue().setEnabled(event.getValue());
                }
            }
        });

        windDirectionSourcesPanel.add(showWindDirectionsSeriesCheckbox);

        for (WindSourceType windSourceType : getWindSourceTypesToOffer()) {
            CheckBox checkbox = dialog.createCheckbox(WindSourceTypeFormatter.format(windSourceType, stringMessages));
            checkbox.setTitle(WindSourceTypeFormatter.tooltipFor(windSourceType, stringMessages));
            checkbox.setEnabled(initialSettings.isShowWindDirectionsSeries());
            windDirectionsCheckboxes.put(windSourceType, checkbox);
            checkbox.setValue(initialSettings.getWindDirectionSourcesToDisplay().contains(windSourceType));
            checkbox.getElement().getStyle().setMarginLeft(15.0, Unit.PX);
            windDirectionSourcesPanel.add(checkbox);
        }
        
        for (WindSourceType windSourceType : getWindSourceTypesToOffer()) {
            CheckBox checkbox = dialog.createCheckbox(WindSourceTypeFormatter.format(windSourceType, stringMessages));
            checkbox.setTitle(WindSourceTypeFormatter.tooltipFor(windSourceType, stringMessages));
            checkbox.setEnabled(initialSettings.isShowWindSpeedSeries() && windSourceType.useSpeed());
            windSpeedCheckboxes.put(windSourceType, checkbox);
            checkbox.setValue(initialSettings.getWindSpeedSourcesToDisplay().contains(windSourceType));
            checkbox.getElement().getStyle().setMarginLeft(15.0, Unit.PX);
            windSpeedSourcesPanel.add(checkbox);
        }
        
        return vp;
    }

    private Iterable<WindSourceType> getWindSourceTypesToOffer() {
        final Set<WindSourceType> result = new HashSet<>();
        result.addAll(Arrays.asList(WindSourceType.values()));
        return result;
    }

    @Override
    public WindChartSettings getResult() {
        Set<WindSourceType> windDirectionSourcesToDisplay = new HashSet<WindSourceType>();
        for (Map.Entry<WindSourceType, CheckBox> e : windDirectionsCheckboxes.entrySet()) {
            if (e.getValue().getValue()) {
                windDirectionSourcesToDisplay.add(e.getKey());
            }
        }
        Set<WindSourceType> windSpeedSourcesToDisplay = new HashSet<WindSourceType>();
        for (Map.Entry<WindSourceType, CheckBox> e : windSpeedCheckboxes.entrySet()) {
            if (e.getValue().getValue()) {
                windSpeedSourcesToDisplay.add(e.getKey());
            }
        }
        return new WindChartSettings(showWindSpeedSeriesCheckbox.getValue(), windSpeedSourcesToDisplay, 
                showWindDirectionsSeriesCheckbox.getValue(), windDirectionSourcesToDisplay, 
                resolutionInSecondsBox.getValue() == null ? -1 : resolutionInSecondsBox.getValue()*1000);
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
        return windDirectionsCheckboxes.values().iterator().next();
    }

}
