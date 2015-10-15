package com.sap.sailing.gwt.ui.datamining.developer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class QueryDefinitionParser {

    private static final String BR = "<br />";
    
    public SafeHtml parseToSafeHtml(StatisticQueryDefinitionDTO queryDefinition) {
        SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
        htmlBuilder.appendEscaped("Locale: " + queryDefinition.getLocaleInfoName()).appendHtmlConstant(BR);
        
        DataRetrieverChainDefinitionDTO chainDefinition = queryDefinition.getDataRetrieverChainDefinition();
        htmlBuilder.appendHtmlConstant(BR)
                   .appendEscaped("Retrieval: " + chainDefinition).appendHtmlConstant(BR)
                   .appendHtmlConstant(TAB(1)).appendEscaped("Levels: ").appendHtmlConstant(BR);
        for (int levelIndex = 0; levelIndex < chainDefinition.getLevelAmount(); levelIndex++) {
            htmlBuilder.appendHtmlConstant(TAB(2)).appendEscaped(levelIndex + ": " + chainDefinition.getRetrieverLevel(levelIndex)).appendHtmlConstant(BR);
        }
         
        HashMap<DataRetrieverLevelDTO, SerializableSettings> retrieverSettings = queryDefinition.getRetrieverSettings();
        if (!retrieverSettings.isEmpty()) {
            // TODO Display retriever settings
        }
        
        HashMap<DataRetrieverLevelDTO,HashMap<FunctionDTO,HashSet<? extends Serializable>>> filterSelection = queryDefinition.getFilterSelection();
        if (!filterSelection.isEmpty()) {
            htmlBuilder.appendHtmlConstant(BR).appendEscaped("Filter Selection:").appendHtmlConstant(BR);
            List<DataRetrieverLevelDTO> retrieverLevels = new ArrayList<>(filterSelection.keySet());
            Collections.sort(retrieverLevels);
            for (DataRetrieverLevelDTO retrieveLevel : retrieverLevels) {
                htmlBuilder.appendHtmlConstant(TAB(1)).appendEscaped("Level " + retrieveLevel.getLevel() + ": ").appendHtmlConstant(BR);
                HashMap<FunctionDTO, HashSet<? extends Serializable>> levelFilterSelection = filterSelection.get(retrieveLevel);
                for (FunctionDTO dimension : levelFilterSelection.keySet()) {
                    htmlBuilder.appendHtmlConstant(TAB(2)).appendEscaped(dimension.toString()).appendHtmlConstant(BR)
                               .appendHtmlConstant(TAB(3)).appendEscaped("Values: ");
                    boolean first = true;
                    for (Serializable value : levelFilterSelection.get(dimension)) {
                        if (!first) {
                            htmlBuilder.appendEscaped(", ");
                        }
                        htmlBuilder.appendEscaped(value.toString());
                        first = false;
                    }
                    htmlBuilder.appendHtmlConstant(BR);
                }
            }
        }

        htmlBuilder.appendHtmlConstant(BR)
                   .appendEscaped("Group By:").appendHtmlConstant(BR);
        ArrayList<FunctionDTO> dimensionsToGroupBy = queryDefinition.getDimensionsToGroupBy();
        for (int index = 0; index < dimensionsToGroupBy.size(); index++) {
            htmlBuilder.appendHtmlConstant(TAB(1)).appendEscaped(index + ": " + dimensionsToGroupBy.get(index)).appendHtmlConstant(BR);
        }
        
        htmlBuilder.appendHtmlConstant(BR)
                   .appendEscaped("Statistic: " + queryDefinition.getStatisticToCalculate()).appendHtmlConstant(BR);
        htmlBuilder.appendEscaped("Aggregator: " + queryDefinition.getAggregatorDefinition());
        
        return htmlBuilder.toSafeHtml();
    }
    
    private String TAB(int indent) {
        StringBuilder tabBuilder = new StringBuilder();
        for (int i = 0; i < indent; i++) {
            tabBuilder.append("&emsp;");
        }
        return tabBuilder.toString();
    }

}
