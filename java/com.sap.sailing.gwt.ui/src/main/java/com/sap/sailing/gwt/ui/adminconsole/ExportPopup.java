package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.trackfiles.TrackFilesDataSource;
import com.sap.sailing.domain.common.trackfiles.TrackFilesExportParameters;
import com.sap.sailing.domain.common.trackfiles.TrackFilesFormat;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * The previously selected Races can now be exported. The user can select the export format, which data to export (wind,
 * buoys tracks, competitor tracks), and what some additional options (data from before / after the start and end of the
 * race, raw or cleansed fixes). This builds the necessary post request that is processed by the gateway bundle.
 * 
 * @author Fredrik Teschke
 * 
 */
public class ExportPopup extends DialogBox {
    private List<RaceDTO> raceDTOs;

    private final FormPanel formPanel;
    private final VerticalPanel mainPanel;

    public ExportPopup(StringMessages stringMessages) {
        super(true);
        getCaption().setText(stringMessages.export());

        formPanel = new FormPanel("_blank");
        formPanel.setAction("/sailingserver/trackfiles/export");
        formPanel.setMethod(FormPanel.METHOD_POST);
        mainPanel = new VerticalPanel();
        formPanel.setWidget(mainPanel);
        add(formPanel);

        ListBox lbFormat = new ListBox();
        lbFormat.setMultipleSelect(false);

        lbFormat.setName(TrackFilesExportParameters.FORMAT);
        lbFormat.setVisibleItemCount(1);
        for (TrackFilesFormat format : TrackFilesFormat.values())
            lbFormat.addItem(format.name());
        mainPanel.add(lbFormat);

        ListBox lbData = new ListBox();
        lbData.setMultipleSelect(true);

        lbData.setName(TrackFilesExportParameters.DATA);
        for (TrackFilesDataSource dataSource : TrackFilesDataSource.values()) {
            lbData.addItem(dataSource.name());
        }
        lbData.setVisibleItemCount(lbData.getItemCount());
        lbData.setItemSelected(0, true);
        mainPanel.add(lbData);

        CheckBox cbBeforeAfter = new CheckBox(stringMessages.dataBeforeAfter());
        cbBeforeAfter.setValue(true);
        cbBeforeAfter.setName(TrackFilesExportParameters.BEFORE_AFTER);
        mainPanel.add(cbBeforeAfter);

        CheckBox cbRawFixes = new CheckBox(stringMessages.rawFixes());
        cbRawFixes.setValue(true);
        cbRawFixes.setName(TrackFilesExportParameters.RAW_FIXES);
        mainPanel.add(cbRawFixes);

        FlowPanel btnPanel = new FlowPanel();
        mainPanel.add(btnPanel);

        Button btnExport = new Button(stringMessages.export());
        btnExport.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                openExportUrl();
                ExportPopup.this.hide();
            }
        });
        btnPanel.add(btnExport);

        Button btnCancel = new Button(stringMessages.cancel());
        btnCancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ExportPopup.this.hide();
            }
        });
        btnPanel.add(btnCancel);
    }

    public void center(List<RaceDTO> raceDTOs) {
        this.raceDTOs = raceDTOs;
        center();
    }

    private void openExportUrl() {
        List<Hidden> races = new ArrayList<Hidden>();
        for (RaceDTO race : raceDTOs) {
            Hidden h = new Hidden(TrackFilesExportParameters.REGATTARACES, race.getRegattaName() + ":" + race.getRaceIdentifier().getRaceName());
            races.add(h);
            mainPanel.add(h);
        }

        formPanel.submit();
        for (Hidden race : races)
            mainPanel.remove(race);
    }
}
