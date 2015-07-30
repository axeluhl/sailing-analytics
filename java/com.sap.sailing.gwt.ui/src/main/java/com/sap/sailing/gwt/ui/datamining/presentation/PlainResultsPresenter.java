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
    
    private final ScrollPanel scrollPanel;
    private final HTML resultsLabel;

    public PlainResultsPresenter(StringMessages stringMessages) {
        super(stringMessages);
        
        resultsLabel = new HTML();
        scrollPanel = new ScrollPanel(resultsLabel);
    }

    @Override
    protected void internalShowResult() {
        QueryResult<Number> result = getCurrentResult();
        Map<GroupKey, Number> resultValues = result.getResults();
        
        StringBuilder resultsBuilder = new StringBuilder("<b>" + result.getResultSignifier() + "</b></ br>");
        
        resultsBuilder.append("<table>");
        for (GroupKey key : getSortedKeys(result)) {
            resultsBuilder.append("<tr>");
            resultsBuilder.append("<td><b>" + key.toString() + "</b>:</td>");
            resultsBuilder.append("<td>" + resultValues.get(key).doubleValue() + "</td>");
            resultsBuilder.append("</tr>");
        }
        resultsBuilder.append("</table>");
        
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
    
}
