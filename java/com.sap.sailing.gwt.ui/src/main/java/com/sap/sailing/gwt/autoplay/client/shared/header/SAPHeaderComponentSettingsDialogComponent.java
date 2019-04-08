package com.sap.sailing.gwt.autoplay.client.shared.header;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.Validator;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class SAPHeaderComponentSettingsDialogComponent implements SettingsDialogComponent<SAPHeaderComponentSettings> {
    private final SAPHeaderComponentSettings initialSettings;
    private TextBox titleTextBox;

    private final StringMessages stringMessages;
    
    public SAPHeaderComponentSettingsDialogComponent(SAPHeaderComponentSettings initialSettings, StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        this.initialSettings = initialSettings;
    }

    @Override 
    public Widget getAdditionalWidget(DataEntryDialog<?> dialog) {
        VerticalPanel vp = new VerticalPanel();

        Grid grid = new Grid(1,2);
        grid.setCellSpacing(5);
        vp.add(grid);
        
        titleTextBox = dialog.createTextBox(initialSettings.getTitle());
        grid.setWidget(0, 0, new Label(stringMessages.title() + ":"));
        grid.setWidget(0, 1, titleTextBox);
        return vp;
    }

    @Override
    public SAPHeaderComponentSettings getResult() {
        return new SAPHeaderComponentSettings(titleTextBox.getValue());
    }

    @Override
    public Validator<SAPHeaderComponentSettings> getValidator() {
        return new Validator<SAPHeaderComponentSettings>() {
            @Override
            public String getErrorMessage(SAPHeaderComponentSettings valueToValidate) {
                return null;
            }
        };
    }

    @Override
    public FocusWidget getFocusWidget() {
        return titleTextBox;
    }

}
