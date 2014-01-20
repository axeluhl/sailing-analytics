package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collections;
import java.util.List;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.ListEditorComposite;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.gwt.ui.DataEntryDialog.DialogCallback;

public class SeriesWithFleetsListEditor extends ListEditorComposite<SeriesDTO> {
    
    public static ListEditorComposite<SeriesDTO> createExpanded(List<SeriesDTO> initialValues, StringMessages stringMessages, ImageResource removeImage) {
        return new SeriesWithFleetsListEditor(initialValues, new ExpandedUi(stringMessages, removeImage));
    }

    protected SeriesWithFleetsListEditor(List<SeriesDTO> initialValues, ListEditorUiStrategy<SeriesDTO> activeUi) {
        super(initialValues, activeUi);
    }
    
    private static class ExpandedUi extends ExpandedListEditorUi<SeriesDTO> {

        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage) {
            super(stringMessages, removeImage);
        }

        @Override
        protected Widget createAddWidget() {
            Button addSeriesButton = new Button(stringMessages.addSeries());
            addSeriesButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    SeriesWithFleetsCreateDialog dialog = new SeriesWithFleetsCreateDialog(Collections
                            .unmodifiableCollection(context.getValue()), stringMessages, new DialogCallback<SeriesDTO>() {
                        @Override
                        public void cancel() {
                        }

                        @Override
                        public void ok(SeriesDTO newSeries) {
                            addValue(newSeries);
                        }
                    });
                    dialog.show();
                }
            });
            return addSeriesButton;
        }

        @Override
        protected Widget createValueWidget(SeriesDTO seriesDTO) {
            Grid seriesGrid = new Grid(2, 3);
            seriesGrid.setCellSpacing(3);
            Label seriesLabel = new Label(stringMessages.series() + ":");
            seriesLabel.setWordWrap(false);
            seriesLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            seriesGrid.setWidget(0, 0, seriesLabel);
            seriesGrid.setHTML(0, 1, seriesDTO.getName());
            if (seriesDTO.getFleets() != null && seriesDTO.getFleets().size() > 0) {
                seriesGrid.setHTML(1, 1, seriesDTO.getFleets().size() + " fleets: "
                        + seriesDTO.getFleets().toString());
            } else {
                seriesGrid.setHTML(1, 1, seriesDTO.getFleets().size() + " No fleets defined.");
            }
            return seriesGrid;

        }
        
    }
    
}
