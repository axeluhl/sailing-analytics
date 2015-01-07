package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Collections;

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
import com.sap.sse.gwt.client.controls.listedit.ExpandedListEditorUi;
import com.sap.sse.gwt.client.controls.listedit.ListEditorComposite;
import com.sap.sse.gwt.client.controls.listedit.ListEditorUiStrategy;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class SeriesWithFleetsListEditor extends ListEditorComposite<SeriesDTO> {

    public SeriesWithFleetsListEditor(Iterable<SeriesDTO> initialValues, StringMessages stringMessages,
            ImageResource removeImage, boolean enableFleetRemoval) {
        this(initialValues, new ExpandedUi(stringMessages, removeImage, enableFleetRemoval));
    }

    protected SeriesWithFleetsListEditor(Iterable<SeriesDTO> initialValues, ListEditorUiStrategy<SeriesDTO> activeUi) {
        super(initialValues, activeUi);
    }

    protected static class ExpandedUi extends ExpandedListEditorUi<SeriesDTO> {
        public ExpandedUi(StringMessages stringMessages, ImageResource removeImage, boolean canRemoveItems) {
            super(stringMessages, removeImage, canRemoveItems);
        }

        @Override
        protected StringMessages getStringMessages() {
            return (StringMessages) super.getStringMessages();
        }

        @Override
        protected Widget createAddWidget() {
            Button addSeriesButton = new Button(getStringMessages().addSeries());
            addSeriesButton.ensureDebugId("AddSeriesButton");
            addSeriesButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    SeriesWithFleetsCreateDialog dialog = new SeriesWithFleetsCreateDialog(Collections
                            .unmodifiableCollection(context.getValue()), getStringMessages(),
                            new DialogCallback<SeriesDTO>() {
                                @Override
                                public void cancel() {
                                }

                                @Override
                                public void ok(SeriesDTO newSeries) {
                                    addValue(newSeries);
                                }
                            });
                    dialog.ensureDebugId("SeriesCreateDialog");
                    dialog.show();
                }
            });
            return addSeriesButton;
        }

        @Override
        protected Widget createValueWidget(int rowIndex, SeriesDTO seriesDTO) {
            HorizontalPanel hPanel = new HorizontalPanel();
            hPanel.setSpacing(5);
            Label seriesLabel = new Label(getStringMessages().series() + " '" + seriesDTO.getName() + "' :");
            seriesLabel.setWordWrap(false);
            seriesLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            hPanel.add(seriesLabel);
            String fleetText = seriesDTO.getFleets().size() + " ";
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
            hPanel.add(new Label(fleetText));
            return hPanel;
        }
    }
}
