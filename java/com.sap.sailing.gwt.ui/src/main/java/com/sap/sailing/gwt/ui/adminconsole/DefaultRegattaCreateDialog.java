package com.sap.sailing.gwt.ui.adminconsole;

import java.util.List;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventAndRegattaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.IconResources;
import com.sap.sse.gwt.client.controls.listedit.ListEditorComposite;

public class DefaultRegattaCreateDialog extends AbstractRegattaWithSeriesAndFleetsDialog<EventAndRegattaDTO> {
    public DefaultRegattaCreateDialog(List<EventDTO> existingEvents, RegattaDTO selectedRegatta,
            SailingServiceAsync sailingService, ErrorReporter errorReporter, StringMessages stringMessages,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<EventAndRegattaDTO> callback) {
        super(sailingService, selectedRegatta, selectedRegatta.series, existingEvents, null,
                stringMessages.createDefaultSettingsForAllRegattas(), stringMessages.ok(), stringMessages,
                null /* RegattaParameterValidator */, callback);
        if (existingEvents != null && !existingEvents.isEmpty()) {
            sailingEventsListBox.addItem((existingEvents.get(0)).getName());
        } else {
            sailingEventsListBox.addItem(stringMessages.noOverallEventSelected());
        }
        sailingEventsListBox.setSelectedIndex(1);
        sailingEventsListBox.setEnabled(false);
        setCourseAreaSelection();
        for (int i=0; i<getRankingMetricListBox().getItemCount(); i++) {
            if (getRankingMetricListBox().getValue(i).equals(selectedRegatta.rankingMetricType.name())) {
                getRankingMetricListBox().setSelectedIndex(i);
            }
        }
    }

    protected ListEditorComposite<SeriesDTO> createSeriesEditor(Iterable<SeriesDTO> series) {
        return new SeriesWithFleetsDefaultListEditor(series, stringMessages, IconResources.INSTANCE.removeIcon(), /* enableFleetRemoval */ false);
    }

    protected void setupAdditionalWidgetsOnPanel(final VerticalPanel panel, Grid formGrid) {
        insertRankingMetricTabPanel(formGrid);
        TabPanel tabPanel = new TabPanel();
        tabPanel.setWidth("100%");
        tabPanel.add(getSeriesEditor(), stringMessages.series());
        tabPanel.selectTab(0);
        panel.add(tabPanel);
    }

    @Override
    protected EventAndRegattaDTO getResult() {
        final RegattaDTO regattaDTO = getRegattaDTO();
        EventAndRegattaDTO eventAndRegatta = new EventAndRegattaDTO(getSelectedEvent(), regattaDTO);
        setRankingMetrics(regattaDTO);
        return eventAndRegatta;
    }
}
