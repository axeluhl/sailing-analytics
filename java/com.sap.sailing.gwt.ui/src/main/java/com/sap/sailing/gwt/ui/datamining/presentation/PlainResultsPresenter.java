package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;

public class PlainResultsPresenter extends AbstractResultsPresenterWithDataProviders<Settings> {
    
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
        SafeHtmlBuilder resultsBuilder = new SafeHtmlBuilder();
        resultsBuilder.appendHtmlConstant("<b>");
        resultsBuilder.appendEscaped(result.getResultSignifier());
        resultsBuilder.appendHtmlConstant("</b></ br>");
        resultsBuilder.appendHtmlConstant("<table>");
        for (GroupKey key : getSortedKeys(result)) {
            resultsBuilder.appendHtmlConstant("<tr>");
            resultsBuilder.appendHtmlConstant("<td><b>");
            resultsBuilder.appendEscaped(key.toString());
            resultsBuilder.appendHtmlConstant("</b>:</td>");
            resultsBuilder.appendHtmlConstant("<td>");
            resultsBuilder.append(resultValues.get(key).doubleValue());
            resultsBuilder.appendHtmlConstant("</td>");
            resultsBuilder.appendHtmlConstant("</tr>");
        }
        resultsBuilder.appendHtmlConstant("</table>");
        resultsLabel.setHTML(resultsBuilder.toSafeHtml().asString());
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

    @Override
    public String getLocalizedShortName() {
        return getStringMessages().plainResultsPresenter();
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<Settings> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Settings newSettings) {
    }

    @Override
    public String getDependentCssClassName() {
        return "plainResultsPresenter";
    }
    
}
