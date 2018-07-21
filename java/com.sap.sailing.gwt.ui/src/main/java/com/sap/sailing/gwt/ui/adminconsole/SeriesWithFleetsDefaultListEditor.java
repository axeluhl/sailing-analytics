package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

/**
 * As opposed to the regular {@link SeriesWithFleetsListEditor}, this variant offers an "Edit Series"
 * button for each series in the list, allowing the user not only to add and remove series but also to
 * edit an existing series in-place.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class SeriesWithFleetsDefaultListEditor extends SeriesWithFleetsListEditor {
    public SeriesWithFleetsDefaultListEditor(Iterable<SeriesDTO> series, StringMessages stringMessages,
            ImageResource removeImage, boolean enableFleetRemoval) {
        super(series, new ExpandedUiDefault(stringMessages, removeImage, enableFleetRemoval));
    }

    private static class ExpandedUiDefault extends ExpandedUi {

        public ExpandedUiDefault(StringMessages stringMessages, ImageResource removeImage, boolean canRemoveItems) {
            super(stringMessages, removeImage, canRemoveItems);
        }

        protected Widget createAddWidget() {
            return new HorizontalPanel();
        }

        private class CompactSeriesPanel extends HorizontalPanel {
            public CompactSeriesPanel(SeriesDTO seriesDTO) {
                setSpacing(5);
                update(seriesDTO);
            }

            public void update(SeriesDTO seriesDTO) {
                clear();
                Label seriesLabel = new Label(getStringMessages().series() + " '" + seriesDTO.getName() + "' :");
                seriesLabel.setWordWrap(false);
                seriesLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
                add(seriesLabel);
                add(new Label(getFleetText(seriesDTO)));
                add(createEditSeriesButton(seriesDTO));
            }

            private String getFleetText(SeriesDTO seriesDTO) {
                String fleetText = "";
                if (seriesDTO.getFleets() != null && !seriesDTO.getFleets().isEmpty()) {
                    if (seriesDTO.getFleets().size() == 1) {
                        fleetText = "1 " + getStringMessages().fleet();
                    } else {
                        fleetText = seriesDTO.getFleets().size() + " " + getStringMessages().fleets();
                    }
                    fleetText += ": " + seriesDTO.getFleets().toString();
                } else {
                    fleetText = getStringMessages().noFleetsDefined();
                }
                return fleetText;
            }

            private Widget createEditSeriesButton(final SeriesDTO seriesDTO) {
                final Button editSeriesButton = new Button(getStringMessages().editSeries());
                editSeriesButton.ensureDebugId("SetDefaultSeriesButton");
                editSeriesButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(final ClickEvent event) {
                        SeriesWithFleetsDefaultCreateDialog dialog = new SeriesWithFleetsDefaultCreateDialog(seriesDTO,
                                getStringMessages(), new DialogCallback<SeriesDTO>() {
                                    @Override
                                    public void cancel() {
                                    }

                                    @Override
                                    public void ok(SeriesDTO newVersionOfEditedSeries) {
                                        // replace the old series by the new version in this editor's list value
                                        final List<SeriesDTO> seriesList = context.getValue();
                                        int oldIndex = seriesList.indexOf(seriesDTO);
                                        seriesList.set(oldIndex, newVersionOfEditedSeries);
                                        update(newVersionOfEditedSeries);
                                    }
                                });
                        dialog.ensureDebugId("SeriesEditDialog");
                        dialog.show();
                    }
                });
                return editSeriesButton;
            }
        }
        
        @Override
        protected Widget createValueWidget(int rowIndex, SeriesDTO seriesDTO) {
            return new CompactSeriesPanel(seriesDTO);
        }
    }
}
