package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.Map.Entry;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;

public class PlainResultsPresenter extends AbstractResultsPresenter<Number> {
    
    private final StringMessages stringMessages;

    private final HTML resultsLabel;

    public PlainResultsPresenter(StringMessages stringMessages) {
        super(stringMessages);
        this.stringMessages = stringMessages;
        
        resultsLabel = new HTML();
    }

    @Override
    public void internalShowResult() {
        QueryResult<Number> result = getCurrentResult();
        StringBuilder resultsBuilder = new StringBuilder("<b>" + result.getResultSignifier() + "</b></ br>");
        resultsBuilder.append(stringMessages.queryResultsChartSubtitle(result.getRetrievedDataAmount(), result.getCalculationTimeInSeconds()));
        
        resultsBuilder.append("<ul>");
        for (Entry<GroupKey, Number> resultEntry : result.getResults().entrySet()) {
            resultsBuilder.append("<li>");
            resultsBuilder.append("<b>" + resultEntry.getKey().toString() + "</b>: ");
            resultsBuilder.append(resultEntry.getValue().doubleValue());
            resultsBuilder.append("</li>");
        }
        resultsBuilder.append("</ul>");
        
        resultsLabel.setHTML(resultsBuilder.toString());
    }
    
    @Override
    protected Widget getPresentationWidget() {
        return resultsLabel;
    }

}
