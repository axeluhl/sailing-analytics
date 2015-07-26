package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;

public class PlainResultsPresenter extends AbstractResultsPresenter<Number> {
    
    private final StringMessages stringMessages;

    private final ScrollPanel scrollPanel;
    private final HTML resultsLabel;

    public PlainResultsPresenter(StringMessages stringMessages) {
        super(stringMessages);
        this.stringMessages = stringMessages;
        
        resultsLabel = new HTML();
        scrollPanel = new ScrollPanel(resultsLabel);
    }

    @Override
    public void internalShowResult() {
        QueryResult<Number> result = getCurrentResult();
        Map<GroupKey, Number> resultValues = result.getResults();
        
        StringBuilder resultsBuilder = new StringBuilder("<b>" + result.getResultSignifier() + "</b></ br>");
        resultsBuilder.append(stringMessages.queryResultsChartSubtitle(result.getRetrievedDataAmount(), result.getCalculationTimeInSeconds()));
        
        resultsBuilder.append("<ul>");
        for (GroupKey key : getSortedKeys(result)) {
            resultsBuilder.append("<li>");
            resultsBuilder.append("<b>" + key.toString() + "</b>: ");
            resultsBuilder.append(resultValues.get(key).doubleValue());
            resultsBuilder.append("</li>");
        }
        resultsBuilder.append("</ul>");
        
        resultsLabel.setHTML(resultsBuilder.toString());
    }
    
    private Iterable<GroupKey> getSortedKeys(QueryResult<Number> result) {
        List<GroupKey> sortedKeys = new ArrayList<>(result.getResults().keySet());
        Collections.sort(sortedKeys);
        return sortedKeys;
    }

    @Override
    protected Widget getPresentationWidget() {
        return scrollPanel;
    }
    
    @Override
    protected Iterable<Widget> getControlWidgets() {
        return new ArrayList<Widget>();
    }

}
