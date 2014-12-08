package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class SeriesWithFleetsDefaultListEditor extends SeriesWithFleetsListEditor {
    private final static Map<String, Map<String, List<SeriesDTO>>> seriesStructure = new HashMap<String, Map<String, List<SeriesDTO>>>();

    public SeriesWithFleetsDefaultListEditor(Iterable<SeriesDTO> series, StringMessages stringMessages,
            ImageResource removeImage, boolean enableFleetRemoval) {
        super(new ArrayList<SeriesDTO>(), new ExpandedUiDefault(stringMessages, removeImage, enableFleetRemoval));
        seriesStructure.clear();
        analyzeSeriesStructure(series);
        List<SeriesDTO> seriesCompact = new ArrayList<SeriesDTO>();
        for (String string : seriesStructure.keySet()) {
            for (String seriesName : seriesStructure.get(string).keySet()) {
                for (SeriesDTO seriesDTO : seriesStructure.get(string).get(seriesName)) {
                    seriesCompact.add(seriesDTO);
                    break;
                }
            }
        }
        super.setValue(seriesCompact);
    }

    private void analyzeSeriesStructure(Iterable<SeriesDTO> series) {
        for (SeriesDTO seriesDTO : series) {
            if (seriesStructure.get(seriesDTO.getName()) == null) {
                seriesStructure.put(seriesDTO.getName(), new HashMap<String, List<SeriesDTO>>());
            }
            String temp = "";
            for (FleetDTO fleet : seriesDTO.getFleets()) {
                temp += fleet.getName();
            }
            if (!seriesStructure.get(seriesDTO.getName()).containsKey(temp)) {
                seriesStructure.get(seriesDTO.getName()).put(temp, new ArrayList<SeriesDTO>());
            }
            seriesStructure.get(seriesDTO.getName()).get(temp).add(seriesDTO);
        }
    }

    private static class ExpandedUiDefault extends ExpandedUi {

        public ExpandedUiDefault(StringMessages stringMessages, ImageResource removeImage, boolean canRemoveItems) {
            super(stringMessages, removeImage, canRemoveItems);
        }

        protected Widget createAddWidget() {
            return new HorizontalPanel();
        }

        private Widget listSeriesWithFleets(final SeriesDTO seriesDTO) {
            final Button editSeriesButton = new Button(getStringMessages().editSeries());
            editSeriesButton.ensureDebugId("SetDefaultSeriesButton");
            editSeriesButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    final Map<String, List<SeriesDTO>> seriesNames = seriesStructure.remove(seriesDTO.getName());
                    String temp = "";
                    for (FleetDTO fleet : seriesDTO.getFleets()) {
                        temp += fleet.getName();
                    }
                    final List<SeriesDTO> oldSeries = seriesNames.remove(temp);
                    SeriesWithFleetsDefaultCreateDialog dialog = new SeriesWithFleetsDefaultCreateDialog(seriesDTO,
                            getStringMessages(), new DialogCallback<SeriesDTO>() {
                                @Override
                                public void cancel() {
                                }

                                @Override
                                public void ok(SeriesDTO defaultSeries) {
                                    for (SeriesDTO series : oldSeries) {
                                        series.setDiscardThresholds(defaultSeries.getDiscardThresholds());
                                        series.setFirstColumnIsNonDiscardableCarryForward(defaultSeries
                                                .isFirstColumnIsNonDiscardableCarryForward());
                                        series.setFleets(defaultSeries.getFleets());
                                        series.setSplitFleetContiguousScoring(defaultSeries
                                                .hasSplitFleetContiguousScoring());
                                        series.setStartsWithZeroScore(defaultSeries.isStartsWithZeroScore());
                                    }
                                    String temp = "";
                                    for (FleetDTO fleet : defaultSeries.getFleets()) {
                                        temp += fleet.getName();
                                    }
                                    seriesNames.put(temp, oldSeries);
                                    seriesStructure.put(defaultSeries.getName(), seriesNames);
                                    HorizontalPanel panel = (HorizontalPanel) editSeriesButton.getParent();
                                    ((Label) panel.getWidget(1)).setText(getFleetText(defaultSeries));
                                }
                            });
                    dialog.ensureDebugId("SeriesEditDialog");
                    dialog.show();
                }
            });
            return editSeriesButton;
        }

        @Override
        protected Widget createValueWidget(int rowIndex, SeriesDTO seriesDTO) {
            HorizontalPanel hPanel = new HorizontalPanel();
            hPanel.setSpacing(5);
            Label seriesLabel = new Label(getStringMessages().series() + " '" + seriesDTO.getName() + "' :");
            seriesLabel.setWordWrap(false);
            seriesLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            hPanel.add(seriesLabel);
            hPanel.add(new Label(getFleetText(seriesDTO)));
            hPanel.add(listSeriesWithFleets(seriesDTO));

            return hPanel;
        }

        protected String getFleetText(SeriesDTO seriesDTO) {
            String fleetText = "";
            if (seriesDTO.getFleets() != null && seriesDTO.getFleets().size() > 0) {
                if (seriesDTO.getFleets().size() == 1) {
                    fleetText = "1 " + getStringMessages().fleet();
                } else {
                    fleetText = seriesDTO.getFleets().size() + " " + getStringMessages().fleets();
                }
                fleetText += ": " + seriesDTO.getFleets().toString();
            } else {
                fleetText = "No fleets defined.";
            }
            return fleetText;
        }
    }
}
