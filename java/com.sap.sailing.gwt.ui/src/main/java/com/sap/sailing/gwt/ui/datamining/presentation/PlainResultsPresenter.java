package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.Map.Entry;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenter;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;

public class PlainResultsPresenter implements ResultsPresenter<Number> {
    
    private final StringMessages stringMessages;

    private final SimplePanel mainPanel;
    private final HTML resultsLabel;

    public PlainResultsPresenter(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        
        mainPanel = new SimplePanel();
        resultsLabel = new HTML();
        mainPanel.setWidget(resultsLabel);
    }

    @Override
    public void showResult(QueryResult<Number> result) {
        StringBuilder resultsBuilder = new StringBuilder("<b>" + result.getResultSignifier() + "</b></ br>");
        resultsBuilder.append(stringMessages.queryResultsChartSubtitle(result.getRetrievedDataAmount(), result.getFilteredDataAmount(), result.getCalculationTimeInSeconds()));
        
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
    public void showError(String error) {
        resultsLabel.setHTML(error);
    }

    @Override
    public void showError(String mainError, Iterable<String> detailedErrors) {
        StringBuilder errorBuilder = new StringBuilder(mainError + ":<br /><ul>");
        for (String detailedError : detailedErrors) {
            errorBuilder.append("<li>" + detailedError + "</li>");
        }
        errorBuilder.append("</ul>");
        showError(errorBuilder.toString());
    }

    @Override
    public Widget getWidget() {
        return mainPanel;
    }

}
