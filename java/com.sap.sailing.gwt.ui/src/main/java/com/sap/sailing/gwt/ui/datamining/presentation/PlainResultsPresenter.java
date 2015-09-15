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
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;

public class PlainResultsPresenter extends AbstractResultsPresenterWithDataProviders {
    
    private final ScrollPanel scrollPanel;
    private final HTML resultsLabel;

    public PlainResultsPresenter(StringMessages stringMessages) {
        super(stringMessages);
        
        resultsLabel = new HTML();
        scrollPanel = new ScrollPanel(resultsLabel);
    }

    @Override
    protected void internalShowNumberResult(Map<GroupKey, Number> resultValues) {
        QueryResultDTO<?> result = getCurrentResult();
        
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
    
    private Iterable<GroupKey> getSortedKeys(QueryResultDTO<?> result) {
        List<GroupKey> sortedKeys = new ArrayList<>(result.getResults().keySet());
        Collections.sort(sortedKeys);
        return sortedKeys;
    }

    @Override
    protected Widget getPresentationWidget() {
        return scrollPanel;
    }
    
}
