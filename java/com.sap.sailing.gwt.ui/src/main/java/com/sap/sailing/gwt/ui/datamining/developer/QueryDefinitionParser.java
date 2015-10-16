package com.sap.sailing.gwt.ui.datamining.developer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.google.gwt.safehtml.shared.OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.sap.sse.common.settings.SerializableSettings;
import com.sap.sse.datamining.shared.dto.StatisticQueryDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverChainDefinitionDTO;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class QueryDefinitionParser {

    public String parseToDetailsAsText(StatisticQueryDefinitionDTO queryDefinition) {
        return parseToDetails(queryDefinition, new TextBuilder());
    }
    
    public SafeHtml parseToDetailsAsSafeHtml(StatisticQueryDefinitionDTO queryDefinition) {
        String safeHtml = parseToDetails(queryDefinition, new HtmlBuilder());
        return new OnlyToBeUsedInGeneratedCodeStringBlessedAsSafeHtml(safeHtml);
    }
    
    private String parseToDetails(StatisticQueryDefinitionDTO queryDefinition, Builder builder) {
        builder.appendText("Locale: " + queryDefinition.getLocaleInfoName()).appendLineBreak();
        
        DataRetrieverChainDefinitionDTO chainDefinition = queryDefinition.getDataRetrieverChainDefinition();
        builder.appendLineBreak()
               .appendText("Retrieval: " + chainDefinition).appendLineBreak()
               .appendTab(1).appendText("Levels: ").appendLineBreak();
        for (int levelIndex = 0; levelIndex < chainDefinition.getLevelAmount(); levelIndex++) {
            builder.appendTab(2).appendText(levelIndex + ": " + chainDefinition.getRetrieverLevel(levelIndex)).appendLineBreak();
        }
         
        HashMap<DataRetrieverLevelDTO, SerializableSettings> retrieverSettings = queryDefinition.getRetrieverSettings();
        if (!retrieverSettings.isEmpty()) {
            // TODO Display retriever settings
        }
        
        HashMap<DataRetrieverLevelDTO,HashMap<FunctionDTO,HashSet<? extends Serializable>>> filterSelection = queryDefinition.getFilterSelection();
        if (!filterSelection.isEmpty()) {
            builder.appendLineBreak().appendText("Filter Selection:").appendLineBreak();
            List<DataRetrieverLevelDTO> retrieverLevels = new ArrayList<>(filterSelection.keySet());
            Collections.sort(retrieverLevels);
            for (DataRetrieverLevelDTO retrieveLevel : retrieverLevels) {
                builder.appendTab(1).appendText("Level " + retrieveLevel.getLevel() + ": ").appendLineBreak();
                HashMap<FunctionDTO, HashSet<? extends Serializable>> levelFilterSelection = filterSelection.get(retrieveLevel);
                for (FunctionDTO dimension : levelFilterSelection.keySet()) {
                    builder.appendTab(2).appendText(dimension.toString()).appendLineBreak()
                           .appendTab(3).appendText("Values: ");
                    boolean first = true;
                    for (Serializable value : levelFilterSelection.get(dimension)) {
                        if (!first) {
                            builder.appendText(", ");
                        }
                        builder.appendText(value.toString());
                        first = false;
                    }
                    builder.appendLineBreak();
                }
            }
        }

        builder.appendLineBreak()
               .appendText("Group By:").appendLineBreak();
        ArrayList<FunctionDTO> dimensionsToGroupBy = queryDefinition.getDimensionsToGroupBy();
        for (int index = 0; index < dimensionsToGroupBy.size(); index++) {
            builder.appendTab(1).appendText(index + ": " + dimensionsToGroupBy.get(index)).appendLineBreak();
        }
        
        builder.appendLineBreak()
               .appendText("Statistic: " + queryDefinition.getStatisticToCalculate()).appendLineBreak();
        builder.appendText("Aggregator: " + queryDefinition.getAggregatorDefinition());
        
        return builder.toString();
    }

    private static final String HTML_LINE_BREAK = "<br />";
    private static final String HTML_TAB = "&emsp;";
    
    private static final String TEXT_LINE_BREAK = "\n";
    private static final String TEXT_TAB = "\t";
    
    private interface Builder {
        
        Builder appendText(String text);
        Builder appendLineBreak();
        Builder appendTab(int tabAmount);
        
        String toString();
        
    }
    
    private class HtmlBuilder implements Builder {
        
        private final SafeHtmlBuilder builder;
        public HtmlBuilder() {
            builder = new SafeHtmlBuilder();
        }

        @Override
        public Builder appendText(String text) {
            builder.appendEscaped(text);
            return this;
        }
        @Override
        public Builder appendLineBreak() {
            builder.appendHtmlConstant(HTML_LINE_BREAK);
            return this;
        }
        @Override
        public Builder appendTab(int tabAmount) {
            builder.appendHtmlConstant(TAB(HTML_TAB, tabAmount));
            return this;
        }
        
        @Override
        public String toString() {
            return builder.toSafeHtml().asString();
        }
        
    }
    
    private class TextBuilder implements Builder {
        
        private final StringBuilder builder;
        public TextBuilder() {
            builder = new StringBuilder();
        }

        @Override
        public Builder appendText(String text) {
            builder.append(text);
            return this;
        }
        @Override
        public Builder appendLineBreak() {
            builder.append(TEXT_LINE_BREAK);
            return this;
        }
        @Override
        public Builder appendTab(int tabAmount) {
            builder.append(TAB(TEXT_TAB, tabAmount));
            return this;
        }
        
        @Override
        public String toString() {
            return builder.toString();
        }
        
    }
    
    private static String TAB(String tabCharacter, int tabAmount) {
        StringBuilder tabBuilder = new StringBuilder();
        for (int i = 0; i < tabAmount; i++) {
            tabBuilder.append(tabCharacter);
        }
        return tabBuilder.toString();
    }

}
