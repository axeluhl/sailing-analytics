package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.EventAndRegattaDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.SeriesDTO;

public class DefaultRegattaCreateDialog extends AbstractRegattaWithSeriesAndFleetsDialog<EventAndRegattaDTO> {

    private static AdminConsoleResources resources = GWT.create(AdminConsoleResources.class);

    private final Iterable<RegattaDTO> defaultRegattas;

    public DefaultRegattaCreateDialog(List<EventDTO> existingEvents, Iterable<RegattaDTO> selectedRegattas,
            SailingServiceAsync sailingService, ErrorReporter errorReporter, StringMessages stringConstants,
            com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback<EventAndRegattaDTO> callback) {
        super(new RegattaDTO(), existingEvents, "Create default settings for all regattas...", stringConstants.ok(),
                stringConstants, null /* RegattaParameterValidator */, callback);
        this.defaultRegattas = selectedRegattas;
        if (existingEvents != null && !existingEvents.isEmpty()) {
            sailingEventsListBox.addItem((existingEvents.get(0)).getName());
        } else {
            sailingEventsListBox.addItem("No overall event selected...");
        }
        sailingEventsListBox.setSelectedIndex(1);
        sailingEventsListBox.setEnabled(false);
        setCourseAreaSelection();
    }

    protected void setSeriesEditor() {
        List<SeriesDTO> series = new ArrayList<SeriesDTO>();
        for (RegattaDTO regattaDTO : defaultRegattas) {
            series.addAll(regattaDTO.series);
        }
        seriesEditor = new SeriesWithFleetsDefaultListEditor(series, stringMessages, resources.removeIcon(), /* enableFleetRemoval */
        false);
    }

    protected void setupAdditionalWidgetsOnPanel(final VerticalPanel panel) {
        TabPanel tabPanel = new TabPanel();
        tabPanel.setWidth("100%");
        tabPanel.add(seriesEditor, stringMessages.series());
        tabPanel.selectTab(0);
        panel.add(tabPanel);
    }

    @Override
    protected EventAndRegattaDTO getResult() {
        EventAndRegattaDTO eventAndRegatta = new EventAndRegattaDTO(super.getSelectedEvent(), super.getRegattaDTO());
        return eventAndRegatta;
    }

}
