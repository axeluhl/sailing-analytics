package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.impl.dto.QueryResultDTO;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class PlainResultsPresenter extends AbstractNumericResultsPresenter<Settings> {
    
    private final ScrollPanel scrollPanel;
    private final HTML resultsLabel;

    public PlainResultsPresenter(Component<?> parent, ComponentContext<?> context, StringMessages stringMessages) {
        super(parent, context, stringMessages);
        
        resultsLabel = new HTML();
        scrollPanel = new ScrollPanel(resultsLabel);
    }

    @Override
    protected void internalShowNumericResult(Map<GroupKey, Number> resultValues, Map<GroupKey, Triple<Number, Number, Long>> errorMargins) {
        QueryResultDTO<?> result = getCurrentResult();
        SafeHtmlBuilder resultsBuilder = new SafeHtmlBuilder();
        resultsBuilder.appendHtmlConstant("<b>").appendEscaped(result.getResultSignifier()).appendHtmlConstant("</b>");
        resultsBuilder.appendHtmlConstant("<br />");
        resultsBuilder.appendHtmlConstant("<table>");
        if (isCurrentResultTwoDimensional()) {
            buildTable(resultValues, resultsBuilder);
        } else {
            buildList(resultValues, resultsBuilder);
        }
        resultsBuilder.appendHtmlConstant("</table>");
        resultsLabel.setHTML(resultsBuilder.toSafeHtml().asString());
    }

    private void buildTable(Map<GroupKey, Number> twoDimensionalResultValues, SafeHtmlBuilder resultsBuilder) {
        Map<GroupKey, Map<GroupKey, Number>> unfoldedResultValues = unfoldResultValues(twoDimensionalResultValues);
        List<GroupKey> sortedMainKeys = new ArrayList<>(unfoldedResultValues.keySet());
        Collections.sort(sortedMainKeys);
        Set<GroupKey> allSubKeys = new HashSet<>();
        unfoldedResultValues.forEach((key, values) -> allSubKeys.addAll(values.keySet()));
        List<GroupKey> sortedSubKeys = new ArrayList<>(allSubKeys);
        Collections.sort(sortedSubKeys);

        resultsBuilder.appendHtmlConstant("<tr><th>&nbsp;</th>"); // First column empty for main keys, but selectable to copy the content
        for (GroupKey subKey : sortedSubKeys) {
            resultsBuilder.appendHtmlConstant("<th>").appendEscaped(subKey.toString()).appendHtmlConstant("</th>");
        }
        resultsBuilder.appendHtmlConstant("</tr>");
        
        for (GroupKey mainKey : sortedMainKeys) {
            resultsBuilder.appendHtmlConstant("<tr>");
            resultsBuilder.appendHtmlConstant("<td><b>").appendEscaped(mainKey + ":").appendHtmlConstant("</b></td>");
            for (GroupKey subKey : sortedSubKeys) {
                Number value = null;
                if (unfoldedResultValues.containsKey(mainKey) &&
                    unfoldedResultValues.get(mainKey).containsKey(subKey)) {
                    value = unfoldedResultValues.get(mainKey).get(subKey);
                }
                resultsBuilder.appendHtmlConstant("<td>");
                if (value != null) {
                    resultsBuilder.append(value.doubleValue());
                }
                resultsBuilder.appendHtmlConstant("</td>");
            }
            resultsBuilder.appendHtmlConstant("</tr>");
        }
    }

    private Map<GroupKey, Map<GroupKey, Number>> unfoldResultValues(Map<GroupKey, Number> twoDimensionalResultValues) {
        Map<GroupKey, Map<GroupKey, Number>> unfoldedResultValues = new HashMap<>();
        for (Entry<GroupKey, Number> entry : twoDimensionalResultValues.entrySet()) {
            List<? extends GroupKey> keys = entry.getKey().getKeys();
            GroupKey mainKey = keys.get(0);
            GroupKey subKey = keys.get(1);
            
            Map<GroupKey, Number> values = unfoldedResultValues.get(mainKey);
            if (values == null) {
                values = new HashMap<>();
                unfoldedResultValues.put(mainKey, values);
            }
            values.put(subKey, entry.getValue());
        }
        return unfoldedResultValues;
    }

    private void buildList(Map<GroupKey, Number> resultValues, SafeHtmlBuilder resultsBuilder) {
        for (GroupKey key : getSortedKeys()) {
            resultsBuilder.appendHtmlConstant("<tr>");
            resultsBuilder.appendHtmlConstant("<td><b>").appendEscaped(key + ":").appendHtmlConstant("</b></td>");
            resultsBuilder.appendHtmlConstant("<td>").append(resultValues.get(key).doubleValue()).appendHtmlConstant("</td>");
            resultsBuilder.appendHtmlConstant("</tr>");
        }
    }
    
    private Iterable<GroupKey> getSortedKeys() {
        List<GroupKey> sortedKeys = new ArrayList<>(getCurrentResult().getResults().keySet());
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
    public SettingsDialogComponent<Settings> getSettingsDialogComponent(Settings settings) {
        return null;
    }

    @Override
    public void updateSettings(Settings newSettings) {
        // no-op
    }

    @Override
    public String getDependentCssClassName() {
        return "plainResultsPresenter";
    }

    @Override
    public Settings getSettings() {
        return null;
    }

    @Override
    public String getId() {
        return "prp";
    }
}
