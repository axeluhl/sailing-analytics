//package com.sap.sailing.gwt.ui.polarmining;
//
//import com.google.gwt.user.client.ui.FocusWidget;
//import com.google.gwt.user.client.ui.Grid;
//import com.google.gwt.user.client.ui.Label;
//import com.google.gwt.user.client.ui.VerticalPanel;
//import com.google.gwt.user.client.ui.Widget;
//import com.sap.sailing.datamining.shared.FoilingSegmentsDataMiningSettings;
//import com.sap.sailing.datamining.shared.TackTypeSegmentsDataMiningSettings;
//import com.sap.sailing.domain.common.impl.KnotSpeedImpl;
//import com.sap.sailing.domain.common.impl.MeterDistance;
//import com.sap.sailing.gwt.ui.client.StringMessages;
//import com.sap.sailing.polars.datamining.shared.PolarDataMiningSettings;
//import com.sap.sse.common.impl.MillisecondsDurationImpl;
//import com.sap.sse.gwt.client.dialog.DataEntryDialog;
//import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
//import com.sap.sse.gwt.client.dialog.DoubleBox;
//import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
//
///**
// * Provides a widget for configuring {@link PolarDataMiningSettings}, including validation.
// * 
// * @author D054528 (Frederik Petersen)
// *
// */
//public class TackTypeSegmentsDataMiningSettingsDialogComponent implements SettingsDialogComponent<TackTypeSegmentsDataMiningSettings> {
//
//    private TackTypeSegmentsDataMiningSettings settings;
//    private StringMessages stringMessages;
//    private DoubleBox minimumTackTypeSegmentsDurationInSecondsBox;
//    private DoubleBox minimumDurationBetweenAdjacentTackTypeSegmentsInSecondsBox;
//
//    public TackTypeSegmentsDataMiningSettingsDialogComponent(TackTypeSegmentsDataMiningSettings settings) {
//        this.settings = settings;
//        this.stringMessages = StringMessages.INSTANCE;
//    }
//
//    @Override
//    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
//        VerticalPanel vp = new VerticalPanel();
//        Grid grid = new Grid(5, 2);
//        grid.setCellPadding(5);
//        vp.add(grid);
//        setupGrid(grid, dialog);
//        return vp;
//    }
//
//    private void setupGrid(Grid grid, DataEntryDialog<?> dialog) {
//        Label minimumFoilingSegmentsDurationInSecondsLabel = new Label(stringMessages.minimumFoilingSegmentsDurationInSeconds() + ":");
//        minimumFoilingSegmentsDurationInSecondsLabel.setTitle(stringMessages.minimumFoilingSegmentsDurationInSecondsTooltip());
//        grid.setWidget(0, 0, minimumFoilingSegmentsDurationInSecondsLabel);
//        if (settings.getMinimumTackTypeSegmentDuration() == null) {
//            minimumTackTypeSegmentsDurationInSecondsBox = dialog.createDoubleBox(6);
//        } else {
//            minimumTackTypeSegmentsDurationInSecondsBox = dialog.createDoubleBox(settings.getMinimumTackTypeSegmentDuration().asSeconds(), 6);
//        }
//        grid.setWidget(0, 1, minimumTackTypeSegmentsDurationInSecondsBox);
//        Label minimumDurationBetweenAdjacentFoilingSegmentsInSecondsBoxLabel = new Label(stringMessages.minimumDurationBetweenAdjacentFoilingSegmentsInSeconds() + ":");
//        minimumDurationBetweenAdjacentFoilingSegmentsInSecondsBoxLabel.setTitle(stringMessages.minimumDurationBetweenAdjacentFoilingSegmentsInSecondsTooltip());
//        grid.setWidget(1, 0, minimumDurationBetweenAdjacentFoilingSegmentsInSecondsBoxLabel);
//        if (settings.getMinimumDurationBetweenAdjacentTackTypeSegments() == null) {
//            minimumDurationBetweenAdjacentTackTypeSegmentsInSecondsBox = dialog.createDoubleBox(6);
//        } else {
//            minimumDurationBetweenAdjacentTackTypeSegmentsInSecondsBox = dialog.createDoubleBox(settings.getMinimumFoilingSegmentDuration().asSeconds(), 6);
//        }
//        grid.setWidget(1, 1, minimumDurationBetweenAdjacentTackTypeSegmentsInSecondsBox);
//        Label maximumSpeedNotFoilingInKnotsLabel = new Label(stringMessages.maximumSpeedNotFoilingInKnots() + ":");
//        maximumSpeedNotFoilingInKnotsLabel.setTitle(stringMessages.maximumSpeedNotFoilingInKnotsTooltip());
//        grid.setWidget(2, 0, maximumSpeedNotFoilingInKnotsLabel);
//        if (settings.getMaximumSpeedNotFoiling() == null) {
//            maximumSpeedNotTackTypeInKnotsBox = dialog.createDoubleBox(6);
//        } else {
//            maximumSpeedNotTackTypeInKnotsBox = dialog.createDoubleBox(settings.getMaximumSpeedNotTackType().getKnots(), 6);
//        }
//        grid.setWidget(2, 1, maximumSpeedNotTackTypeInKnotsBox);
//        Label minimumSpeedForFoilingInKnotsLabel = new Label(stringMessages.minimumSpeedForFoilingInKnots() + ":");
//        minimumSpeedForFoilingInKnotsLabel.setTitle(stringMessages.minimumSpeedForFoilingInKnotsTooltip());
//        grid.setWidget(3, 0, minimumSpeedForFoilingInKnotsLabel);
//        if (settings.getMinimumSpeedForTackType() == null) {
//            minimumSpeedForTackTypeInKnotsBox = dialog.createDoubleBox(6);
//        } else {
//            minimumSpeedForTackTypeInKnotsBox = dialog.createDoubleBox(settings.getMinimumSpeedForTackType().getKnots(), 6);
//        }
//        grid.setWidget(3, 1, minimumSpeedForTackTypeInKnotsBox);
//        Label minimumRideHeightInMetersLabel = new Label(stringMessages.minimumRideHeightInMeters() + ":");
//        minimumRideHeightInMetersLabel.setTitle(stringMessages.minimumRideHeightInMetersTooltip());
//        grid.setWidget(4, 0, minimumRideHeightInMetersLabel);
//        if (settings.getMinimumRideHeight() == null) {
//            minimumRideHeightInMetersBox = dialog.createDoubleBox(6);
//        } else {
//            minimumRideHeightInMetersBox = dialog.createDoubleBox(settings.getMinimumRideHeight().getMeters(), 6);
//        }
//        grid.setWidget(4, 1, minimumRideHeightInMetersBox);
//    }
//
//    @Override
//    public FoilingSegmentsDataMiningSettings getResult() {
//        return new FoilingSegmentsDataMiningSettings(
//                minimumTackTypeSegmentsDurationInSecondsBox.getValue() == null ? null
//                        : new MillisecondsDurationImpl((long) (minimumTackTypeSegmentsDurationInSecondsBox.getValue() * 1000.)),
//                minimumDurationBetweenAdjacentTackTypeSegmentsInSecondsBox.getValue() == null ? null
//                        : new MillisecondsDurationImpl((long) (minimumDurationBetweenAdjacentTackTypeSegmentsInSecondsBox.getValue() * 1000.)),
//                minimumSpeedForTackTypeInKnotsBox.getValue() == null ? null
//                        : new KnotSpeedImpl(minimumSpeedForFoilingInKnotsBox.getValue()),
//                maximumSpeedNotFoilingInKnotsBox.getValue() == null ? null
//                        : new KnotSpeedImpl(maximumSpeedNotFoilingInKnotsBox.getValue()), minimumRideHeightInMetersBox.getValue() == null ? null
//                        : new MeterDistance(minimumRideHeightInMetersBox.getValue()));
//    }
//
//    @Override
//    public FocusWidget getFocusWidget() {
//        return minimumTackTypeSegmentsDurationInSecondsBox;
//    }
//
//    @Override
//    public Validator<FoilingSegmentsDataMiningSettings> getValidator() {
//        return new FoilingSegmentsDataMiningSettingsValidator(stringMessages);
//    }
//
//}
