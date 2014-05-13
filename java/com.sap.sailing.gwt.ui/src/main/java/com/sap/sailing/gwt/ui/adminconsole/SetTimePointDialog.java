package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Date;

import com.github.gwtbootstrap.datetimepicker.client.ui.base.HasViewMode.ViewMode;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.BetterDateTimeBox;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

public class SetTimePointDialog extends DataEntryDialog<Date> {
    private final BetterDateTimeBox time;
    private final StringMessages stringMessages;
    
    public SetTimePointDialog(final StringMessages stringMessages, String title, DialogCallback<Date> callback) {
        super(title, title, stringMessages.ok(), stringMessages.cancel(), new DataEntryDialog.Validator<Date>() {
            @Override
            public String getErrorMessage(Date valueToValidate) {
                if (valueToValidate == null) return stringMessages.pleaseEnterA(stringMessages.time());
                return null;
            }
        }, callback);
        
        this.stringMessages = stringMessages;
        
        time = createTimeBox();
    }
    
    private BetterDateTimeBox createTimeBox() {
        final BetterDateTimeBox timeBox = new BetterDateTimeBox();
        timeBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
            @Override
            public void onValueChange(ValueChangeEvent<Date> event) {
                validate();
            }
        });
        timeBox.addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    addAutoHidePartner(timeBox.getPicker());
                }
            }
        });
        timeBox.setAutoClose(true);
        timeBox.setStartView(ViewMode.HOUR);
        timeBox.setFormat("dd/mm/yyyy hh:ii");
        return timeBox;
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        Grid grid = new Grid(1, 2);
        grid.setWidget(0, 0, new Label(stringMessages.time()));
        grid.setWidget(0, 1, time);
        return grid;
    }
    
    @Override
    protected Date getResult() {
        return time.getValue();
    }

}
