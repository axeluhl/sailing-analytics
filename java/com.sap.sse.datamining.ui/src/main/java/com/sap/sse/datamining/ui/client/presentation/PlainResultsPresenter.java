package com.sap.sse.datamining.ui.client.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class PlainResultsPresenter extends AbstractNumericResultsPresenter<Settings> {

    private static final String htmlWhitespace = "&nbsp;";

    private final ScrollPanel scrollPanel;
    private final HTML resultsLabel;

    private final LinkedHashSet<String> signifiers;
    private final Map<GroupKey, Map<String, Number>> results;

    public PlainResultsPresenter(Component<?> parent, ComponentContext<?> context) {
        super(parent, context);
        signifiers = new LinkedHashSet<>();
        results = new HashMap<>();

        resultsLabel = new HTML();
        scrollPanel = new ScrollPanel(resultsLabel);
    }

    @Override
    protected void internalShowNumericResults(Map<GroupKey, Number> resultValues,
            Map<GroupKey, Triple<Number, Number, Long>> errorMargins) {
        signifiers.clear();
        results.clear();

        String currentSignifier = getCurrentResult().getResultSignifier();
        signifiers.add(currentSignifier);
        for (Entry<GroupKey, Number> entry : resultValues.entrySet()) {
            Map<String, Number> values = results.get(entry.getKey());
            if (values == null) {
                values = new HashMap<>();
                results.put(entry.getKey(), values);
            }
            values.put(currentSignifier, entry.getValue());
        }

        SafeHtmlBuilder displayBuilder = new SafeHtmlBuilder();
        if (signifiers.size() == 1) {
            displayBuilder.appendHtmlConstant("<b>").appendEscaped(getCurrentResult().getResultSignifier())
                    .appendHtmlConstant("</b>").appendHtmlConstant("<br />");
        }

        displayBuilder.appendHtmlConstant("<table>");
        if (signifiers.size() > 1) {
            buildTableForMultipleSignifiers(displayBuilder);
        } else if (areCollectedResultsTwoDimensional()) {
            buildTableForTwoDimensionalResult(displayBuilder);
        } else {
            buildList(displayBuilder);
        }
        displayBuilder.appendHtmlConstant("</table>");
        resultsLabel.setHTML(displayBuilder.toSafeHtml().asString());
    }

    private boolean areCollectedResultsTwoDimensional() {
        for (GroupKey key : results.keySet()) {
            if (key.size() != 2) {
                return false;
            }
        }
        return true;
    }

    private void buildTableForMultipleSignifiers(SafeHtmlBuilder displayBuilder) {
        buildTableHeader(signifiers, displayBuilder);
        for (GroupKey key : getSortedKeys(results.keySet())) {
            buildTableRow(key, signifiers, results.get(key), displayBuilder);
        }
    }

    private void buildTableForTwoDimensionalResult(SafeHtmlBuilder displayBuilder) {
        Map<GroupKey, Map<GroupKey, Number>> unfoldedResultValues = unfoldResultValues();
        Set<GroupKey> allSubKeys = new HashSet<>();
        unfoldedResultValues.forEach((key, values) -> allSubKeys.addAll(values.keySet()));
        Iterable<GroupKey> sortedSubKeys = getSortedKeys(allSubKeys);

        buildTableHeader(sortedSubKeys, displayBuilder);
        for (GroupKey mainKey : getSortedKeys(unfoldedResultValues.keySet())) {
            buildTableRow(mainKey, sortedSubKeys, unfoldedResultValues.get(mainKey), displayBuilder);
        }
    }

    private void buildTableHeader(Iterable<?> columnHeaders, SafeHtmlBuilder displayBuilder) {
        displayBuilder.appendHtmlConstant("<tr><th>" + htmlWhitespace + "</th>"); // First column empty, but selectable
                                                                                  // to copy the content without
                                                                                  // shifting the headers
        for (Object columnHeader : columnHeaders) {
            displayBuilder.appendHtmlConstant("<th>").appendEscaped(columnHeader.toString())
                    .appendHtmlConstant("</th>");
        }
        displayBuilder.appendHtmlConstant("</tr>");
    }

    private void buildTableRow(GroupKey rowKey, Iterable<?> orderedColumnKeys, Map<?, Number> columnValues,
            SafeHtmlBuilder displayBuilder) {
        displayBuilder.appendHtmlConstant("<tr>");
        displayBuilder.appendHtmlConstant("<td><b>").appendEscaped(rowKey + ":").appendHtmlConstant("</b></td>");
        for (Object columnKey : orderedColumnKeys) {
            Number value = columnValues.get(columnKey);
            displayBuilder.appendHtmlConstant("<td>");
            if (value != null) {
                displayBuilder.append(value.doubleValue());
            }
            displayBuilder.appendHtmlConstant("</td>");
        }
        displayBuilder.appendHtmlConstant("</tr>");
    }

    private Map<GroupKey, Map<GroupKey, Number>> unfoldResultValues() {
        String signifier = Util.first(signifiers);
        Map<GroupKey, Map<GroupKey, Number>> unfoldedResultValues = new HashMap<>();
        for (Entry<GroupKey, Map<String, Number>> entry : results.entrySet()) {
            List<? extends GroupKey> keys = entry.getKey().getKeys();
            GroupKey mainKey = keys.get(0);
            GroupKey subKey = keys.get(1);

            Map<GroupKey, Number> values = unfoldedResultValues.get(mainKey);
            if (values == null) {
                values = new HashMap<>();
                unfoldedResultValues.put(mainKey, values);
            }
            values.put(subKey, entry.getValue().get(signifier));
        }
        return unfoldedResultValues;
    }

    private void buildList(SafeHtmlBuilder displayBuilder) {
        String signifier = Util.first(signifiers);
        for (GroupKey key : getSortedKeys(results.keySet())) {
            displayBuilder.appendHtmlConstant("<tr>");
            displayBuilder.appendHtmlConstant("<td><b>").appendEscaped(key + ":").appendHtmlConstant("</b></td>");
            displayBuilder.appendHtmlConstant("<td>").append(results.get(key).get(signifier).doubleValue())
                    .appendHtmlConstant("</td>");
            displayBuilder.appendHtmlConstant("</tr>");
        }
    }

    private Iterable<GroupKey> getSortedKeys(Set<GroupKey> keys) {
        List<GroupKey> sortedKeys = new ArrayList<>(keys);
        Collections.sort(sortedKeys);
        return sortedKeys;
    }

    @Override
    protected Widget getPresentationWidget() {
        return scrollPanel;
    }

    @Override
    public String getLocalizedShortName() {
        return getDataMiningStringMessages().plainResultsPresenter();
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
