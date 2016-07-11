package com.sap.sailing.gwt.ui.client.media;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class MediaPlayerSettingsDialogComponent implements SettingsDialogComponent<MediaPlayerSettings> {
    private final MediaPlayerSettings initialSettings;
    private CheckBox autoPlayMediaCheckbox;

    private final StringMessages stringMessages;
    
    public MediaPlayerSettingsDialogComponent(MediaPlayerSettings initialSettings, StringMessages stringMessages) {
        this.initialSettings = initialSettings;
        this.stringMessages = stringMessages;
    }

    @Override 
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();

        Grid grid = new Grid(1,2);
        grid.setCellSpacing(5);
        vp.add(grid);
        
        autoPlayMediaCheckbox = dialog.createCheckbox("");
        autoPlayMediaCheckbox.setValue(initialSettings.isAutoSelectMedia());
        
        grid.setWidget(0, 0, new Label(stringMessages.autoPlayMedia() + ":"));
        grid.setWidget(0, 1, autoPlayMediaCheckbox);
        return vp;
    }

    @Override
    public MediaPlayerSettings getResult() {
        return new MediaPlayerSettings(autoPlayMediaCheckbox.getValue());
    }

    @Override
    public Validator<MediaPlayerSettings> getValidator() {
        return new Validator<MediaPlayerSettings>() {
            @Override
            public String getErrorMessage(MediaPlayerSettings valueToValidate) {
                return null;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return autoPlayMediaCheckbox;
    }
}
