package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.gwt.ui.client.DataEntryDialogWithBootstrap;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class AbstractPairingListCreationDialog<T> extends DataEntryDialogWithBootstrap<T> {
    
    protected StringMessages stringMessages;

    public AbstractPairingListCreationDialog(RegattaIdentifier regattaIdentifier, String title, StringMessages stringMessages,
            Validator<T> validator, DialogCallback<T> callback) {
        
        super(title, null, stringMessages.ok(), stringMessages.cancel(), validator, callback);
        this.stringMessages = stringMessages;
        
        
    }
    
    @Override
    protected Widget getAdditionalWidget() {
        final VerticalPanel panel = new VerticalPanel();
        
        return panel;
    }

    @Override
    protected T getResult() {
        return null;
    }
}
