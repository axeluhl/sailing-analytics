package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;
import java.util.Collections;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class SeriesWithFleetsDefaultCreateDialog extends SeriesWithFleetsCreateDialog {
    public SeriesWithFleetsDefaultCreateDialog(SeriesDTO defaultSeries, StringMessages stringMessages,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<SeriesDTO> callback) {
        super(Collections.<SeriesDTO> emptyList(), stringMessages, defaultSeries.getDiscardThresholds(), callback);
        nameEntryField.setText(defaultSeries.getName());// Otherwise an errorMessage will pop up
        isMedalSeriesCheckbox.setValue(defaultSeries.isFirstColumnIsNonDiscardableCarryForward());
        startsWithZeroScoreCheckbox.setValue(defaultSeries.isStartsWithZeroScore());
        hasSplitFleetContiguousScoringCheckbox.setValue(defaultSeries.hasSplitFleetContiguousScoring());
        firstColumnIsNonDiscardableCarryForwardCheckbox.setValue(defaultSeries.isFirstColumnIsNonDiscardableCarryForward());
        useSeriesResultDiscardingThresholdsCheckbox.setValue(defaultSeries.definesSeriesDiscardThresholds());
        fleetListComposite.setValue(defaultSeries.getFleets());
    }

    @Override
    protected Widget getAdditionalWidget() {
        VerticalPanel panel = (VerticalPanel) super.getAdditionalWidget();
        Grid formGrid = (Grid) panel.getWidget(0);
        formGrid.removeRow(0);
        formGrid.removeRow(0);
        return panel;
    }

    protected void initializeFleetListComposite(StringMessages stringMessages) {
        fleetListComposite = new FleetListEditableEditorComposite(Arrays.asList(new FleetDTO(
                LeaderboardNameConstants.DEFAULT_FLEET_NAME, 0, null)), stringMessages, resources.removeIcon());
        fleetListComposite.ensureDebugId("FleetListEditableEditorComposite");
        fleetListComposite.addValueChangeHandler(new ValueChangeHandler<Iterable<FleetDTO>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Iterable<FleetDTO>> event) {
                validate();
            }
        });
    }
}
