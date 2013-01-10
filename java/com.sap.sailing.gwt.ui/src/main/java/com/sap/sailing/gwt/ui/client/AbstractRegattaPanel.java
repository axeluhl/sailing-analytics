package com.sap.sailing.gwt.ui.client;

import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.client.ui.FormPanel;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public abstract class AbstractRegattaPanel extends FormPanel implements RegattaDisplayer {
    protected final SailingServiceAsync sailingService;
    protected DateTimeFormatRenderer dateFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
    protected DateTimeFormatRenderer timeFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG));
    protected final RegattaRefresher regattaRefresher;
    protected ErrorReporter errorReporter;
    protected StringMessages stringMessages;
    
    public AbstractRegattaPanel(SailingServiceAsync sailingService,
            RegattaRefresher regattaRefresher, ErrorReporter errorReporter, StringMessages stringMessages) {
        super();
        this.sailingService = sailingService;
        this.regattaRefresher = regattaRefresher;
        this.errorReporter  = errorReporter;
        this.stringMessages = stringMessages;
    }

    @Override
    public abstract void fillRegattas(List<RegattaDTO> result);
    
    /**
     * Returns <code>true</code> if <code>wordsToFilter</code> contain a value of the <code>valuesToCheck</code>
     * 
     * @param wordsToFilter
     *            the words to filter on
     * @param valuesToCheck
     *            the values to check for. These values contain the values of the current rows.
     * @return <code>true</code> if the <code>valuesToCheck</code> contains all <code>wordsToFilter</code>,
     *         <code>false</code> if not
     */
    protected boolean textContainsStringsToCheck(List<String> wordsToFilter, String... valuesToCheck) {
        boolean found = true;
        for (String word : wordsToFilter) {
            String textAsUppercase = word.toUpperCase().trim();
            boolean notContainedinEveryValue = true;
            for (int i = 0; notContainedinEveryValue && i < valuesToCheck.length; i++) {
                String string = valuesToCheck[i];
                notContainedinEveryValue = string==null || !string.toUpperCase().contains(textAsUppercase);
            }
            if (notContainedinEveryValue) {
                found = false;
                break;
            }
        }
        return found;
    }
}
