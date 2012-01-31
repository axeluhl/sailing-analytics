package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.client.ui.FormPanel;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public abstract class AbstractEventPanel extends FormPanel implements EventDisplayer {
    protected final SailingServiceAsync sailingService;
    protected DateTimeFormatRenderer dateFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
    protected DateTimeFormatRenderer timeFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG));
    protected final EventRefresher eventRefresher;
    protected ErrorReporter errorReporter;
    protected StringMessages stringConstants;
    
    public AbstractEventPanel(SailingServiceAsync sailingService,
            EventRefresher eventRefresher, ErrorReporter errorReporter, StringMessages stringConstants) {
        super();
        this.sailingService = sailingService;
        this.eventRefresher = eventRefresher;
        this.errorReporter  = errorReporter;
        this.stringConstants = stringConstants;
    }

    @Override
    public abstract void fillEvents(List<EventDTO> result);
    
    /**
     * Returns <code>false</code> if <code>wordsToFilter</code> contain a value of the <code>valuesToCheck</code>  
     * @param wordsToFilter the words to filter on
     * @param valuesToCheck the values to check for. These values contain the values of the current rows.
     * @return <code>false</code> if the <code>valuesToCheck</code> contains all <code>wordsToFilter</code>, <code>true</code> if not
     */
    protected boolean textContainingStringsToCheck(List<String> wordsToFilter, String... valuesToCheck){
        boolean failed = false;
        for (String word : wordsToFilter) {
            String textAsUppercase = word.toUpperCase().trim();
            boolean notContainedinEveryValue = true;
            for (int i = 0; notContainedinEveryValue && i < valuesToCheck.length; i++) {
                String string = valuesToCheck[i];
                notContainedinEveryValue = !string.toUpperCase().contains(textAsUppercase);
            }
            if(notContainedinEveryValue){
                failed = true;
                break;
            }
        }
        return failed;
    }
}
