package com.sap.sailing.gwt.ui.client;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.client.ui.FormPanel;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleView.Presenter;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.gwt.client.ErrorReporter;

//TODO: Do not inherit from FormPanel since the provided functionality is never used!
public abstract class AbstractRegattaPanel extends FormPanel implements RegattasDisplayer {
    protected final SailingServiceWriteAsync sailingServiceWrite;
    protected DateTimeFormatRenderer dateFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
    protected DateTimeFormatRenderer timeFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG));
    protected final Presenter presenter;
    protected ErrorReporter errorReporter;
    protected StringMessages stringMessages;
    
    public AbstractRegattaPanel(Presenter presenter, StringMessages stringMessages) {
        super();
        this.sailingServiceWrite = presenter.getSailingService();
        this.presenter = presenter;
        this.errorReporter  = presenter.getErrorReporter();
        this.stringMessages = stringMessages;
    }

    @Override
    public abstract void fillRegattas(Iterable<RegattaDTO> result);
    
}
