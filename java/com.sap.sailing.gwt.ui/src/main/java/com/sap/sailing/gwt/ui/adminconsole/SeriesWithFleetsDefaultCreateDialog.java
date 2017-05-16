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
import com.sap.sse.gwt.client.IconResources;

/**
 * A variant of the dialog used to create a series within a regatta that has fleets. This variant is actually more of a
 * series <em>editor</em> that doesn't allow for the full-fledged race sequence editing but initializes its controls
 * from an already existing series and allows users to change them. The {@link SeriesDTO} object returned from
 * {@link #getResult()} will be a new one, distinct from the {@link SeriesDTO} one passed to the constructor, but as the
 * UI controls have been initialized from the latter, in case the user makes no change the resulting {@link SeriesDTO}
 * will resemble the one passed to the constructor.
 * 
 * @author Nils Hirsekorn, Axel Mueller
 * @author Axel Uhl (D043530)
 *
 */
public class SeriesWithFleetsDefaultCreateDialog extends SeriesWithFleetsCreateDialog {
    public SeriesWithFleetsDefaultCreateDialog(SeriesDTO defaultSeries, StringMessages stringMessages,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<SeriesDTO> callback) {
        super(Collections.<SeriesDTO> emptyList(), stringMessages, defaultSeries.getDiscardThresholds(), callback);
        nameEntryField.setText(defaultSeries.getName()); // Otherwise an errorMessage will pop up
        isMedalSeriesCheckbox.setValue(defaultSeries.isFirstColumnIsNonDiscardableCarryForward());
        fleetsCanRunInParallelCheckbox.setValue(defaultSeries.isFleetsCanRunInParallel());
        startsWithZeroScoreCheckbox.setValue(defaultSeries.isStartsWithZeroScore());
        hasSplitFleetContiguousScoringCheckbox.setValue(defaultSeries.hasSplitFleetContiguousScoring());
        firstColumnIsNonDiscardableCarryForwardCheckbox.setValue(defaultSeries.isFirstColumnIsNonDiscardableCarryForward());
        maximumNumberOfDiscardsBox.setValue(defaultSeries.getMaximumNumberOfDiscards());
        useSeriesResultDiscardingThresholdsCheckbox.setValue(defaultSeries.definesSeriesDiscardThresholds(), /* fire events */ true);
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
                LeaderboardNameConstants.DEFAULT_FLEET_NAME, 0, null)), stringMessages, IconResources.INSTANCE.removeIcon());
        fleetListComposite.ensureDebugId("FleetListEditableEditorComposite");
        fleetListComposite.addValueChangeHandler(new ValueChangeHandler<Iterable<FleetDTO>>() {
            @Override
            public void onValueChange(ValueChangeEvent<Iterable<FleetDTO>> event) {
                validateAndUpdate();
            }
        });
    }
}
